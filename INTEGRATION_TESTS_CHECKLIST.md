# Integration Tests Checklist - Core Banking System

Este documento lista los endpoints que requieren tests de integración, ordenados por prioridad, con las ramificaciones (casos de test) que debe cubrir cada uno.

## Resumen de Cobertura

| Módulo | Endpoints | Tests Existentes | Tests Requeridos | Cobertura |
|--------|-----------|------------------|------------------|-----------|
| Auth | 5 | 11 | 30 | 37% |
| Customer | 6 | 3 | 33 | 9% |
| Account | 7 | 0 | 39 | 0% |
| Transaction | 5 | 0 | 46 | 0% |
| Transfers | 3 | 0 | 29 | 0% |
| **TOTAL** | **26** | **14** | **177** | **8%** |

## Orden de Prioridad de Implementación

```
1. Auth (Fundación) ──► 2. Customer (KYC) ──► 3. Account ──► 4. Transaction/Transfers
```

---

## TIER 1: AUTH MODULE `/api/v1/auth`

**Prioridad:** MÁXIMA - Fundación del sistema, todos los módulos dependen de auth.

### 1.1 POST `/register` - Registro de Usuario

| # | Caso de Test | Tipo | Complejidad | Estado |
|---|--------------|------|-------------|--------|
| 1 | Registro exitoso con datos válidos | Success | Medium | ✅ DONE |
| 2 | Email con formato inválido | Validation | Low | ✅ DONE |
| 3 | Contraseña muy corta (<8 chars) | Validation | Low | ✅ DONE |
| 4 | Campos requeridos faltantes | Validation | Low | ✅ DONE |
| 5 | Email duplicado (409 Conflict) | Business Rule | Medium | ✅ DONE |
| 6 | Número de documento duplicado | Business Rule | Medium | ✅ DONE |
| 7 | Usuario menor de edad (birthDate) | Validation | Low | ✅ DONE|
| 8 | Formato de teléfono inválido | Validation | Low | ✅ DONE |

**Archivo de test:** `AuthRestControllerIT.java`

---

### 1.2 POST `/login` - Inicio de Sesión

| # | Caso de Test | Tipo | Complejidad | Estado |
|---|--------------|------|-------------|--------|
| 1 | Login exitoso, retorna JWT token | Success | Low | ✅ DONE |
| 2 | Contraseña incorrecta (401) | Auth Error | Low | ✅ DONE |
| 3 | Usuario no existe (401) | Auth Error | Low | ✅ DONE |
| 4 | Email con formato inválido | Validation | Low | ✅ DONE |
| 5 | Contraseña corta en request | Validation | Low | ✅ DONE |
| 6 | Usuario con email no verificado | Auth Error | Medium | ✅ DONE |
| 7 | Rate limiting excedido (429) | Security | High | ✅ DONE |

---

### 1.3 POST `/verify-email` - Verificación de Email

| # | Caso de Test | Tipo | Complejidad | Estado |
|---|--------------|------|-------------|--------|
| 1 | Verificación exitosa con token válido | Success | Medium | ✅ DONE |
| 2 | Token de verificación inválido | Auth Error | Low | ✅ DONE |
| 3 | Token de verificación expirado | Auth Error | Medium | ✅ DONE |
| 4 | Usuario ya verificado previamente | Business Rule | Low | ✅ DONE |

---

### 1.4 POST `/resend-verification` - Reenvío de Verificación

| # | Caso de Test | Tipo | Complejidad | Estado |
|---|--------------|------|-------------|--------|
| 1 | Reenvío exitoso a usuario no verificado | Success | Medium | ⬜ PENDING |
| 2 | Usuario no encontrado | Not Found | Low | ⬜ PENDING |
| 3 | Usuario ya verificado | Business Rule | Low | ⬜ PENDING |
| 4 | Email con formato inválido | Validation | Low | ⬜ PENDING |

---

### 1.5 PUT `/change-password` - Cambio de Contraseña (Autenticado)

| # | Caso de Test | Tipo | Complejidad | Estado |
|---|--------------|------|-------------|--------|
| 1 | Cambio de contraseña exitoso | Success | Medium | ⬜ PENDING |
| 2 | Contraseña actual incorrecta | Auth Error | Low | ⬜ PENDING |
| 3 | Nueva contraseña igual a la actual | Validation | Low | ⬜ PENDING |
| 4 | Nueva contraseña muy corta | Validation | Low | ⬜ PENDING |
| 5 | Sin autenticación (401) | Auth Error | Low | ⬜ PENDING |
| 6 | Token JWT expirado | Auth Error | Medium | ⬜ PENDING |

---

## TIER 2: CUSTOMER MODULE `/api/v1/customers`

**Prioridad:** ALTA - KYC es prerequisito para cuentas y transacciones.

**Regla crítica:** El estado KYC (`PENDING` → `APPROVED`/`REJECTED`) controla el acceso a operaciones financieras.

### 2.1 GET `/me` - Obtener Mi Perfil

| # | Caso de Test | Tipo | Complejidad | Estado |
|---|--------------|------|-------------|--------|
| 1 | Obtener perfil con auth válida | Success | Low | ✅ DONE |
| 2 | Sin autenticación (401) | Auth Error | Low | ✅ DONE |
| 3 | Customer no encontrado para userId | Not Found | Low | ✅ DONE |
| 4 | Sin authority `CUSTOMER_VIEW_OWN` (403) | Forbidden | Low | ⬜ PENDING |

**Archivo de test:** `CustomerRestControllerTestIT.java`

---

### 2.2 PUT `/me` - Actualizar Mi Perfil

| # | Caso de Test | Tipo | Complejidad | Estado |
|---|--------------|------|-------------|--------|
| 1 | Actualizar teléfono exitosamente | Success | Medium | ⬜ PENDING |
| 2 | Actualizar dirección exitosamente | Success | Medium | ⬜ PENDING |
| 3 | **Actualizar nombre resetea KYC a PENDING** | Business Rule | High | ⬜ PENDING |
| 4 | Sin autenticación (401) | Auth Error | Low | ⬜ PENDING |
| 5 | Sin authority `CUSTOMER_UPDATE` (403) | Forbidden | Low | ⬜ PENDING |
| 6 | Customer no encontrado | Not Found | Low | ⬜ PENDING |
| 7 | Formato de teléfono inválido | Validation | Low | ⬜ PENDING |
| 8 | Request body inválido | Validation | Low | ⬜ PENDING |

---

### 2.3 GET `/{customerId}` - Obtener Customer por ID (Admin)

| # | Caso de Test | Tipo | Complejidad | Estado |
|---|--------------|------|-------------|--------|
| 1 | Obtener customer con authority admin | Success | Medium | ⬜ PENDING |
| 2 | Sin autenticación (401) | Auth Error | Low | ⬜ PENDING |
| 3 | Sin authority `CUSTOMER_VIEW` (403) | Forbidden | Low | ⬜ PENDING |
| 4 | Customer no encontrado (404) | Not Found | Low | ⬜ PENDING |
| 5 | Formato UUID inválido | Validation | Low | ⬜ PENDING |

---

### 2.4 GET `/` - Listar Todos los Customers (Admin)

| # | Caso de Test | Tipo | Complejidad | Estado |
|---|--------------|------|-------------|--------|
| 1 | Obtener lista paginada | Success | Medium | ⬜ PENDING |
| 2 | Lista vacía (sin customers) | Success | Low | ⬜ PENDING |
| 3 | Sin autenticación (401) | Auth Error | Low | ⬜ PENDING |
| 4 | Sin authority `CUSTOMER_VIEW_ALL` (403) | Forbidden | Low | ⬜ PENDING |
| 5 | Parámetros de paginación custom | Success | Low | ⬜ PENDING |

---

### 2.5 PUT `/{customerId}/kyc/approve` - Aprobar KYC (Admin)

| # | Caso de Test | Tipo | Complejidad | Estado |
|---|--------------|------|-------------|--------|
| 1 | Aprobar KYC de customer PENDING | Success | Medium | ⬜ PENDING |
| 2 | Customer no encontrado (404) | Not Found | Low | ⬜ PENDING |
| 3 | KYC ya está APPROVED (idempotente o error) | Business Rule | Medium | ⬜ PENDING |
| 4 | KYC está REJECTED (transición inválida?) | Business Rule | Medium | ⬜ PENDING |
| 5 | Sin authority `KYC_APPROVE` (403) | Forbidden | Low | ⬜ PENDING |
| 6 | Sin autenticación (401) | Auth Error | Low | ⬜ PENDING |

---

### 2.6 PUT `/{customerId}/kyc/reject` - Rechazar KYC (Admin)

| # | Caso de Test | Tipo | Complejidad | Estado |
|---|--------------|------|-------------|--------|
| 1 | Rechazar KYC de customer PENDING | Success | Medium | ⬜ PENDING |
| 2 | Customer no encontrado (404) | Not Found | Low | ⬜ PENDING |
| 3 | KYC ya está REJECTED (idempotente o error) | Business Rule | Medium | ⬜ PENDING |
| 4 | KYC está APPROVED (transición inválida?) | Business Rule | Medium | ⬜ PENDING |
| 5 | Sin authority `KYC_REJECT` (403) | Forbidden | Low | ⬜ PENDING |
| 6 | Sin autenticación (401) | Auth Error | Low | ⬜ PENDING |

---

## TIER 3: ACCOUNT MODULE `/api/v1/accounts`

**Prioridad:** ALTA - Requerido para transacciones.

**Reglas críticas:**
- ⚠️ KYC debe estar APPROVED para crear cuenta
- ⚠️ Solo UNA cuenta USD por customer
- ⚠️ Cuenta debe estar ACTIVE para operaciones

### 3.1 POST `/` - Crear Cuenta

| # | Caso de Test | Tipo | Complejidad | Estado |
|---|--------------|------|-------------|--------|
| 1 | **Crear cuenta con KYC aprobado** | Success | High | ⬜ PENDING |
| 2 | Crear primera cuenta USD | Success | Medium | ⬜ PENDING |
| 3 | **Crear segunda cuenta USD (bloqueado)** | Business Rule | High | ⬜ PENDING |
| 4 | **KYC no aprobado (error)** | Business Rule | Medium | ⬜ PENDING |
| 5 | Customer no encontrado para userId | Not Found | Low | ⬜ PENDING |
| 6 | Sin authority `ACCOUNT_CREATE` (403) | Forbidden | Low | ⬜ PENDING |
| 7 | Sin autenticación (401) | Auth Error | Low | ⬜ PENDING |
| 8 | Tipo de cuenta inválido | Validation | Low | ⬜ PENDING |
| 9 | Código de moneda inválido | Validation | Low | ⬜ PENDING |
| 10 | Campos requeridos faltantes | Validation | Low | ⬜ PENDING |

---

### 3.2 GET `/me` - Obtener Mis Cuentas

| # | Caso de Test | Tipo | Complejidad | Estado |
|---|--------------|------|-------------|--------|
| 1 | Obtener todas mis cuentas | Success | Medium | ⬜ PENDING |
| 2 | Usuario sin cuentas (lista vacía) | Success | Low | ⬜ PENDING |
| 3 | Customer no encontrado | Not Found | Low | ⬜ PENDING |
| 4 | Sin authority `ACCOUNT_VIEW_OWN` (403) | Forbidden | Low | ⬜ PENDING |
| 5 | Sin autenticación (401) | Auth Error | Low | ⬜ PENDING |

---

### 3.3 GET `/me/{accountId}` - Obtener Mi Cuenta Específica

| # | Caso de Test | Tipo | Complejidad | Estado |
|---|--------------|------|-------------|--------|
| 1 | Obtener mi cuenta por ID | Success | Medium | ⬜ PENDING |
| 2 | Cuenta no encontrada (404) | Not Found | Low | ⬜ PENDING |
| 3 | **Cuenta pertenece a otro usuario (403)** | Forbidden | Medium | ⬜ PENDING |
| 4 | Customer no encontrado | Not Found | Low | ⬜ PENDING |
| 5 | Sin autenticación (401) | Auth Error | Low | ⬜ PENDING |
| 6 | Formato UUID inválido | Validation | Low | ⬜ PENDING |

---

### 3.4 GET `/{accountId}` - Obtener Cualquier Cuenta (Admin)

| # | Caso de Test | Tipo | Complejidad | Estado |
|---|--------------|------|-------------|--------|
| 1 | Obtener cuenta con auth admin | Success | Medium | ⬜ PENDING |
| 2 | Cuenta no encontrada (404) | Not Found | Low | ⬜ PENDING |
| 3 | Sin authority `ACCOUNT_VIEW_ALL` (403) | Forbidden | Low | ⬜ PENDING |
| 4 | Sin autenticación (401) | Auth Error | Low | ⬜ PENDING |
| 5 | Formato UUID inválido | Validation | Low | ⬜ PENDING |

---

### 3.5 GET `/me/{accountId}/balance` - Obtener Balance

| # | Caso de Test | Tipo | Complejidad | Estado |
|---|--------------|------|-------------|--------|
| 1 | Obtener balance de mi cuenta | Success | Medium | ⬜ PENDING |
| 2 | Cuenta no encontrada (404) | Not Found | Low | ⬜ PENDING |
| 3 | **Cuenta pertenece a otro usuario (403)** | Forbidden | Medium | ⬜ PENDING |
| 4 | Customer no encontrado | Not Found | Low | ⬜ PENDING |
| 5 | Sin autenticación (401) | Auth Error | Low | ⬜ PENDING |
| 6 | Sin authority `ACCOUNT_VIEW_OWN` (403) | Forbidden | Low | ⬜ PENDING |

---

### 3.6 GET `/search` - Buscar Cuenta por Alias

| # | Caso de Test | Tipo | Complejidad | Estado |
|---|--------------|------|-------------|--------|
| 1 | Buscar cuenta por alias (encontrada) | Success | Medium | ⬜ PENDING |
| 2 | Alias no encontrado (404) | Not Found | Low | ⬜ PENDING |
| 3 | Sin autenticación (401) | Auth Error | Low | ⬜ PENDING |
| 4 | Parámetro alias vacío | Validation | Low | ⬜ PENDING |
| 5 | Parámetro alias en blanco | Validation | Low | ⬜ PENDING |

---

### 3.7 GET `/types` - Obtener Tipos de Cuenta

| # | Caso de Test | Tipo | Complejidad | Estado |
|---|--------------|------|-------------|--------|
| 1 | Obtener todos los tipos de cuenta | Success | Low | ⬜ PENDING |
| 2 | Sin autenticación (401) | Auth Error | Low | ⬜ PENDING |

---

## TIER 4A: TRANSACTION MODULE `/api/v1/transactions`

**Prioridad:** CRÍTICA - Operaciones financieras core.

**Reglas críticas:**
- ⚠️ KYC debe estar APPROVED
- ⚠️ Cuenta debe estar ACTIVE
- ⚠️ Idempotency key previene transacciones duplicadas
- ⚠️ Moneda debe coincidir con la cuenta
- ⚠️ Fondos suficientes para retiros

### 4.1 POST `/accounts/{accountId}/deposits` - Depósito

| # | Caso de Test | Tipo | Complejidad | Estado |
|---|--------------|------|-------------|--------|
| 1 | **Depósito exitoso** | Success | High | ⬜ PENDING |
| 2 | **Verificar balance actualizado** | Success | High | ⬜ PENDING |
| 3 | **Idempotencia - misma key retorna éxito** | Business Rule | High | ⬜ PENDING |
| 4 | **Idempotencia - key duplicada, request diferente** | Business Rule | High | ⬜ PENDING |
| 5 | **KYC no aprobado** | Business Rule | Medium | ⬜ PENDING |
| 6 | **Cuenta no activa** | Business Rule | Medium | ⬜ PENDING |
| 7 | **Moneda no coincide con cuenta** | Business Rule | Medium | ⬜ PENDING |
| 8 | **Cuenta pertenece a otro usuario** | Forbidden | Medium | ⬜ PENDING |
| 9 | Cuenta no encontrada (404) | Not Found | Low | ⬜ PENDING |
| 10 | Customer no encontrado | Not Found | Low | ⬜ PENDING |
| 11 | Monto cero | Validation | Low | ⬜ PENDING |
| 12 | Monto negativo | Validation | Low | ⬜ PENDING |

---

### 4.2 POST `/accounts/{accountId}/withdrawals` - Retiro

| # | Caso de Test | Tipo | Complejidad | Estado |
|---|--------------|------|-------------|--------|
| 1 | **Retiro exitoso** | Success | High | ⬜ PENDING |
| 2 | **Verificar balance actualizado** | Success | High | ⬜ PENDING |
| 3 | **Fondos insuficientes** | Business Rule | High | ⬜ PENDING |
| 4 | **Idempotencia - misma key retorna éxito** | Business Rule | High | ⬜ PENDING |
| 5 | **Idempotencia - key duplicada, request diferente** | Business Rule | High | ⬜ PENDING |
| 6 | **KYC no aprobado** | Business Rule | Medium | ⬜ PENDING |
| 7 | **Cuenta no activa** | Business Rule | Medium | ⬜ PENDING |
| 8 | **Moneda no coincide con cuenta** | Business Rule | Medium | ⬜ PENDING |
| 9 | **Cuenta pertenece a otro usuario** | Forbidden | Medium | ⬜ PENDING |
| 10 | Cuenta no encontrada (404) | Not Found | Low | ⬜ PENDING |
| 11 | Customer no encontrado | Not Found | Low | ⬜ PENDING |
| 12 | Monto cero | Validation | Low | ⬜ PENDING |
| 13 | Monto negativo | Validation | Low | ⬜ PENDING |
| 14 | **Retiro exacto del balance completo** | Edge Case | Medium | ⬜ PENDING |

---

### 4.3 GET `/me` - Historial de Transacciones del Customer

| # | Caso de Test | Tipo | Complejidad | Estado |
|---|--------------|------|-------------|--------|
| 1 | Obtener historial de transacciones | Success | Medium | ⬜ PENDING |
| 2 | Historial vacío (sin transacciones) | Success | Low | ⬜ PENDING |
| 3 | Paginación funciona correctamente | Success | Medium | ⬜ PENDING |
| 4 | Ordenado por `executed_at` descendente | Success | Medium | ⬜ PENDING |
| 5 | KYC no aprobado | Business Rule | Medium | ⬜ PENDING |
| 6 | Customer no encontrado | Not Found | Low | ⬜ PENDING |
| 7 | Sin autenticación (401) | Auth Error | Low | ⬜ PENDING |

---

### 4.4 GET `/accounts/{accountId}` - Historial de Transacciones de Cuenta

| # | Caso de Test | Tipo | Complejidad | Estado |
|---|--------------|------|-------------|--------|
| 1 | Obtener transacciones de mi cuenta | Success | Medium | ⬜ PENDING |
| 2 | **Cuenta pertenece a otro usuario (403)** | Forbidden | Medium | ⬜ PENDING |
| 3 | Cuenta no encontrada (404) | Not Found | Low | ⬜ PENDING |
| 4 | Historial vacío | Success | Low | ⬜ PENDING |
| 5 | Parámetros de paginación | Success | Medium | ⬜ PENDING |
| 6 | Sin autenticación (401) | Auth Error | Low | ⬜ PENDING |
| 7 | KYC no aprobado | Business Rule | Medium | ⬜ PENDING |

---

### 4.5 GET `/{transactionId}` - Obtener Transacción por ID

| # | Caso de Test | Tipo | Complejidad | Estado |
|---|--------------|------|-------------|--------|
| 1 | Obtener mi transacción por ID | Success | Medium | ⬜ PENDING |
| 2 | Transacción no encontrada (404) | Not Found | Low | ⬜ PENDING |
| 3 | **Transacción pertenece a otro usuario (403)** | Forbidden | Medium | ⬜ PENDING |
| 4 | Customer no encontrado | Not Found | Low | ⬜ PENDING |
| 5 | Sin autenticación (401) | Auth Error | Low | ⬜ PENDING |
| 6 | KYC no aprobado | Business Rule | Medium | ⬜ PENDING |

---

## TIER 4B: TRANSFERS MODULE `/api/v1/transfers`

**Prioridad:** CRÍTICA - Transferencias entre cuentas.

**Reglas críticas:**
- ⚠️ KYC debe estar APPROVED para cuenta origen
- ⚠️ Cuentas origen y destino deben ser diferentes
- ⚠️ Monedas deben ser compatibles
- ⚠️ Fondos suficientes en cuenta origen
- ⚠️ Exactamente UNO de `toAlias` o `toAccountNumber`
- ⚠️ Idempotency key previene transferencias duplicadas

### 4.6 POST `/` - Crear Transferencia

| # | Caso de Test | Tipo | Complejidad | Estado |
|---|--------------|------|-------------|--------|
| 1 | **Transferencia por alias exitosa** | Success | High | ⬜ PENDING |
| 2 | **Transferencia por número de cuenta exitosa** | Success | High | ⬜ PENDING |
| 3 | **Verificar balance origen decrementado** | Success | High | ⬜ PENDING |
| 4 | **Verificar balance destino incrementado** | Success | High | ⬜ PENDING |
| 5 | **Idempotencia - misma key retorna existente** | Business Rule | High | ⬜ PENDING |
| 6 | **Fondos insuficientes** | Business Rule | High | ⬜ PENDING |
| 7 | **Transferencia a misma cuenta (bloqueado)** | Business Rule | High | ⬜ PENDING |
| 8 | **Monedas incompatibles entre cuentas** | Business Rule | High | ⬜ PENDING |
| 9 | **KYC no aprobado** | Business Rule | Medium | ⬜ PENDING |
| 10 | Cuenta origen no encontrada (404) | Not Found | Low | ⬜ PENDING |
| 11 | Cuenta destino no encontrada (alias) | Not Found | Low | ⬜ PENDING |
| 12 | Cuenta destino no encontrada (número) | Not Found | Low | ⬜ PENDING |
| 13 | **Ambos alias Y número de cuenta provistos** | Validation | Medium | ⬜ PENDING |
| 14 | **Ni alias NI número de cuenta provistos** | Validation | Medium | ⬜ PENDING |
| 15 | **Cuenta origen pertenece a otro usuario** | Forbidden | Medium | ⬜ PENDING |
| 16 | Monto cero | Validation | Low | ⬜ PENDING |
| 17 | Monto negativo | Validation | Low | ⬜ PENDING |
| 18 | Transferencia con fee/comisión | Success | High | ⬜ PENDING |

---

### 4.7 GET `/me/{id}` - Obtener Mi Transferencia

| # | Caso de Test | Tipo | Complejidad | Estado |
|---|--------------|------|-------------|--------|
| 1 | Obtener transferencia como emisor | Success | Medium | ⬜ PENDING |
| 2 | Obtener transferencia como receptor | Success | Medium | ⬜ PENDING |
| 3 | Transferencia no encontrada (404) | Not Found | Low | ⬜ PENDING |
| 4 | **Usuario no es emisor ni receptor (403)** | Forbidden | Medium | ⬜ PENDING |
| 5 | Customer no encontrado | Not Found | Low | ⬜ PENDING |
| 6 | Sin autenticación (401) | Auth Error | Low | ⬜ PENDING |

---

### 4.8 GET `/{id}` - Obtener Cualquier Transferencia (Admin)

| # | Caso de Test | Tipo | Complejidad | Estado |
|---|--------------|------|-------------|--------|
| 1 | Obtener transferencia con auth admin | Success | Medium | ⬜ PENDING |
| 2 | Transferencia no encontrada (404) | Not Found | Low | ⬜ PENDING |
| 3 | Sin authority `TRANSACTION_VIEW_ALL` (403) | Forbidden | Low | ⬜ PENDING |
| 4 | Sin autenticación (401) | Auth Error | Low | ⬜ PENDING |
| 5 | Formato UUID inválido | Validation | Low | ⬜ PENDING |

---

## Leyenda

| Símbolo | Significado |
|---------|-------------|
| ✅ DONE | Test implementado |
| ⬜ PENDING | Test pendiente |
| ⚠️ | Regla de negocio crítica |
| **Negrita** | Caso de alta prioridad |

## Tipos de Test

| Tipo | Descripción |
|------|-------------|
| Success | Flujo feliz, operación exitosa |
| Validation | Error de validación de input (400) |
| Auth Error | Error de autenticación (401) |
| Forbidden | Error de autorización (403) |
| Not Found | Recurso no encontrado (404) |
| Business Rule | Violación de regla de negocio |
| Edge Case | Caso límite o especial |
| Security | Relacionado con seguridad |

## Complejidad

| Nivel | Descripción |
|-------|-------------|
| Low | Test simple, mock básico |
| Medium | Requiere setup de datos, múltiples asserts |
| High | Setup complejo, múltiples entidades, verificación de estado |

## Archivos de Referencia

```
src/test/java/com/banking/system/integration/
├── AbstractIntegrationTest.java          # Base class con TestContainers
├── auth/
│   └── AuthRestControllerIT.java         # Patrón a seguir
├── customer/
│   └── CustomerRestControllerTestIT.java # Tests existentes
└── examples/
    └── SecurityTestExamples.java         # Ejemplos de auth en tests
```

## Tests por Prioridad

### CRÍTICOS (Implementar primero)
- Crear cuenta con KYC
- Límite de una cuenta USD
- Depósito/Retiro con verificación de balance
- Idempotencia en transacciones
- Fondos insuficientes
- Transferencia entre cuentas
- Validación de propiedad de cuenta

### ALTOS
- Todos los casos de autenticación (401)
- Todos los casos de autorización (403)
- Validación de KYC en operaciones
- Búsqueda por alias

### MEDIOS
- Validaciones de input
- Paginación
- Edge cases
