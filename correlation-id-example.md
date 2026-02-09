# Correlation ID - Ejemplo Concreto

## Escenario: Usuario hace una transferencia

### 1. Request HTTP llega
```http
POST /api/v1/transactions/transfer
Headers:
  Authorization: Bearer eyJhbG...

Body:
{
  "fromAccount": "ACC001",
  "toAccount": "ACC002",
  "amount": 1000.00
}
```

### 2. CorrelationIdFilter genera ID
```
correlationId = "7f3a9b2c-e4d1-4c2a-9b3f-1a2b3c4d5e6f"
↓
Agrega al MDC (contexto de logging)
```

### 3. Código de negocio ejecuta y loguea

**TransferService.java:**
```java
@Service
@Slf4j
public class TransferService {

    public void transfer(TransferRequest request) {
        log.info("Starting transfer");  // Log 1

        Account fromAccount = accountRepo.findById(request.getFromAccount());
        log.info("Source account found: {}", fromAccount.getId());  // Log 2

        validateBalance(fromAccount, request.getAmount());
        log.info("Balance validated");  // Log 3

        Account toAccount = accountRepo.findById(request.getToAccount());
        log.info("Destination account found: {}", toAccount.getId());  // Log 4

        // PROBLEMA: NullPointerException
        String ownerName = toAccount.getOwner().getName();  // ¡Error!
        log.error("Transfer failed", ex);  // Log 5
    }
}
```

### 4. Logs generados (TODOS con el mismo correlationId)

**Sin correlation ID (ACTUAL - DIFÍCIL DE RASTREAR):**
```
2026-02-09 15:30:01 INFO  Starting transfer
2026-02-09 15:30:01 INFO  Starting transfer    ← ¿De otro usuario?
2026-02-09 15:30:01 INFO  Source account found: ACC001
2026-02-09 15:30:01 INFO  Source account found: ACC789  ← ¿De otro usuario?
2026-02-09 15:30:01 INFO  Balance validated
2026-02-09 15:30:01 INFO  Destination account found: ACC002
2026-02-09 15:30:01 ERROR Transfer failed: NullPointerException  ← ¿Cuál transferencia?
```

**Con correlation ID (NUEVO - FÁCIL DE RASTREAR):**
```
2026-02-09 15:30:01 INFO  [7f3a9b2c] Starting transfer
2026-02-09 15:30:01 INFO  [a1b2c3d4] Starting transfer    ← Otro usuario
2026-02-09 15:30:01 INFO  [7f3a9b2c] Source account found: ACC001
2026-02-09 15:30:01 INFO  [a1b2c3d4] Source account found: ACC789
2026-02-09 15:30:01 INFO  [7f3a9b2c] Balance validated
2026-02-09 15:30:01 INFO  [7f3a9b2c] Destination account found: ACC002
2026-02-09 15:30:01 ERROR [7f3a9b2c] Transfer failed: NullPointerException
```

### 5. Usuario recibe respuesta de error

```json
{
  "timestamp": "2026-02-09T15:30:01Z",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred. Please contact support with the correlation ID.",
  "correlationId": "7f3a9b2c-e4d1-4c2a-9b3f-1a2b3c4d5e6f"
}
```

### 6. Usuario contacta soporte

**Usuario:** "Mi transferencia falló. Correlation ID: 7f3a9b2c-e4d1-4c2a-9b3f-1a2b3c4d5e6f"

**Tú (desarrollador):**
```bash
# Búsqueda instantánea de TODOS los logs de ese request
grep "7f3a9b2c" /var/log/banking.log

# Resultado: TODOS los logs del request del usuario
[7f3a9b2c] Starting transfer
[7f3a9b2c] Source account found: ACC001
[7f3a9b2c] Balance validated
[7f3a9b2c] Destination account found: ACC002
[7f3a9b2c] Transfer failed: NullPointerException at TransferService.java:45
```

**Encuentras el problema en 5 segundos** ✅

---

## Correlation ID vs Transaction ID

| Concepto | Qué es | Alcance | Ejemplo |
|----------|--------|---------|---------|
| **Correlation ID** | ID del REQUEST HTTP | 1 request HTTP completo | `7f3a9b2c-...` |
| **Transaction ID** | ID de la transacción de negocio | La entidad de negocio (Transaction) | `TXN-2026-001234` |

**Un request puede generar múltiples transacciones:**
```
Correlation ID: 7f3a9b2c
├── Transaction TXN-001 (debit from ACC001)
├── Transaction TXN-002 (credit to ACC002)
└── AuditLog (registro de auditoría)
```

**Pero todos los logs de ese request tienen el mismo Correlation ID.**

---

## ¿Necesito logback-spring.xml?

**NO es obligatorio** para que funcione el correlation ID.

El código que implementé YA funciona porque:
1. El filtro agrega el correlationId al MDC
2. Spring Boot ya incluye Logback por defecto
3. Tus logs con `@Slf4j` ya funcionan

**PERO**, logback-spring.xml te permite:**

### Sin logback-spring.xml (comportamiento actual):
```
2026-02-09 15:30:01 INFO  c.b.s.t.service.TransferService - Starting transfer
```

### Con logback-spring.xml (mejorado):
```
2026-02-09 15:30:01 INFO  [correlationId=7f3a9b2c] c.b.s.t.service.TransferService - Starting transfer
```

---

## Conclusión

✅ **Ya implementado y funcionando:**
- Correlation ID se genera automáticamente
- Se incluye en respuestas de error
- MDC configurado

⚠️ **Opcional (pero recomendado):**
- Configurar logback-spring.xml para ver el correlationId en los logs de consola/archivo

**¿Quieres que cree el logback-spring.xml para que veas el correlation ID en tus logs?**