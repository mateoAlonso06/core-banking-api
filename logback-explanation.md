# ¿Por qué XML en Java?

## Respuesta corta:
**Historia y convención.** Java tiene +25 años, y XML era el estándar de configuración en los 2000s.

## Cronología:

```
2000s → XML era "moderno"
├── Spring: applicationContext.xml
├── Hibernate: hibernate.cfg.xml
├── Maven: pom.xml
└── Logback: logback.xml

2010s → Anotaciones (@Configuration) + YAML/Properties
├── Spring Boot: application.yml
├── @SpringBootApplication, @Bean, etc.
└── Pero logback sigue usando XML (legacy)

2020s → JSON, YAML más populares
└── Logback SIGUE usando XML (compatibilidad)
```

## ¿Por qué Logback no cambió?

1. **Compatibilidad hacia atrás**: Millones de proyectos usan XML
2. **No hay beneficio suficiente**: Cambiar a YAML no agrega funcionalidad
3. **XML funciona bien**: Es verboso pero claro y validable con XSD

## Alternativas modernas:

Logback SÍ soporta Groovy (2012+):
```groovy
// logback.groovy
appender("CONSOLE", ConsoleAppender) {
  encoder(PatternLayoutEncoder) {
    pattern = "%d [%X{correlationId}] %msg%n"
  }
}
```

Pero casi nadie lo usa porque XML "ya funciona" y hay más documentación.

---

## ¿Qué hace el logback-spring.xml que creé?

### Línea clave:
```xml
<pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%X{correlationId:-NO-CORRELATION-ID}] %-5level %logger{36} - %msg%n</pattern>
```

**Desglose:**
- `%d{...}` = Fecha y hora
- `%X{correlationId}` = Lee del MDC (lo que el filtro puso)
- `:-NO-CORRELATION-ID` = Valor por defecto si no hay correlationId
- `%-5level` = Nivel de log (INFO, ERROR, etc.)
- `%logger{36}` = Nombre de la clase (máximo 36 caracteres)
- `%msg` = Tu mensaje
- `%n` = Nueva línea

### Resultado:

**Antes (sin logback-spring.xml):**
```
2026-02-09 15:30:01.123 INFO  c.b.s.t.service.TransferService - Starting transfer
2026-02-09 15:30:01.456 ERROR c.b.s.t.service.TransferService - Transfer failed
```

**Después (con logback-spring.xml):**
```
2026-02-09 15:30:01.123 [7f3a9b2c-e4d1-4c2a-9b3f-1a2b3c4d5e6f] INFO  c.b.s.t.service.TransferService - Starting transfer
2026-02-09 15:30:01.456 [7f3a9b2c-e4d1-4c2a-9b3f-1a2b3c4d5e6f] ERROR c.b.s.t.service.TransferService - Transfer failed
```

Ahora puedes hacer:
```bash
grep "7f3a9b2c" logs/app.log  # Encuentra TODOS los logs de ese request
```

### Diferencias dev vs prod:

**Development:**
- Logs legibles para humanos
- Muestra SQL queries
- Level INFO (más verboso)

**Production:**
- Logs en JSON (para parsers automáticos)
- Oculta SQL queries
- Level WARN (menos ruido)
- Ejemplo:
  ```json
  {"timestamp":"2026-02-09T15:30:01.123-0300","correlationId":"7f3a9b2c","level":"ERROR","logger":"c.b.s.t.service.TransferService","message":"Transfer failed"}
  ```

---

## Resumen:

- **¿Por qué XML?** → Historia de Java (2000s), compatibilidad
- **¿Puedo usar YAML?** → No para Logback (solo XML o Groovy)
- **¿Importa?** → No, XML funciona perfecto y hay toneladas de ejemplos
- **¿Qué gano?** → Ahora ves `[correlationId]` en TODOS tus logs