# Production Readiness Checklist

Este documento describe los requisitos y mejores prácticas necesarias para desplegar el Core Banking System en producción de forma segura y confiable.

## Tabla de Contenidos

- [Estado Actual](#estado-actual)
- [Áreas Críticas](#áreas-críticas)
  - [1. Seguridad](#1-seguridad)
  - [2. Validación y Manejo de Errores](#2-validación-y-manejo-de-errores)
  - [3. Base de Datos en Producción](#3-base-de-datos-en-producción)
  - [4. Configuración por Ambiente](#4-configuración-por-ambiente)
  - [5. Observabilidad y Debugging](#5-observabilidad-y-debugging)
  - [6. Resiliencia](#6-resiliencia)
  - [7. Email/Notificaciones](#7-emailnotificaciones)
  - [8. CI/CD y Deployment](#8-cicd-y-deployment)
  - [9. Documentación Operacional](#9-documentación-operacional)
- [Checklist Completo](#checklist-completo)
- [Priorización](#priorización)

---

## Estado Actual

### ✅ Lo que ya está implementado

- **Rate limiting**: Redis + Bucket4j con algoritmo token bucket
- **Seguridad básica**: JWT authentication, BCrypt password hashing, Spring Security 6
- **Migraciones**: Flyway consolidado (V1 schema, V2 seed data)
- **Containerización**: Docker multi-stage build + Docker Compose
- **Monitoring básico**: Spring Boot Actuator con health checks, métricas y Prometheus endpoint
- **Arquitectura limpia**: Hexagonal Architecture con separación de capas
- **Testing**: JUnit 5 + TestContainers para integration tests
- **Perfiles de ambiente**: `application-dev.yml` y `application-prod.yml`

---

## Áreas Críticas

### 1. Seguridad

#### 1.1 HTTPS/TLS Obligatorio

**Estado:** ❌ No configurado

**Problema:** El sistema actualmente acepta HTTP, lo cual expone credenciales y tokens en texto plano.

**Acción requerida:**
```yaml
# application-prod.yml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: tomcat
  port: 8443

# Forzar HTTPS redirect
security:
  require-ssl: true
```

**Alternativa:** Usar un reverse proxy (Nginx, ALB) que maneje TLS termination.

#### 1.2 Secrets Management

**Estado:** ⚠️ Usa variables de entorno (OK para dev, NO para prod)

**Problema:** Los secrets en `.env` o variables de entorno están expuestos en logs, procesos, etc.

**Acción requerida:**
- **AWS:** Usar AWS Secrets Manager o Parameter Store
- **Kubernetes:** Usar Kubernetes Secrets + External Secrets Operator
- **General:** HashiCorp Vault

**Ejemplo con AWS Secrets Manager:**
```java
// Agregar dependencia spring-cloud-aws-starter-secrets-manager
// Los secrets se inyectan automáticamente desde AWS
```

#### 1.3 Security Headers

**Estado:** ⚠️ Configuración parcial

**Acción requerida:**
```java
// SecurityConfig.java
http.headers(headers -> headers
    .contentSecurityPolicy(csp -> csp
        .policyDirectives("default-src 'self'; frame-ancestors 'none';"))
    .httpStrictTransportSecurity(hsts -> hsts
        .includeSubDomains(true)
        .maxAgeInSeconds(31536000))
    .frameOptions(frame -> frame.deny())
    .xssProtection(xss -> xss.block(true))
);
```

#### 1.4 Audit Logging

**Estado:** ❌ Módulo scaffolded pero no implementado

**Acción requerida:**
- Implementar interceptor que registre:
  - Quién (userId)
  - Qué (acción: CREATE_ACCOUNT, APPROVE_KYC, TRANSFER, etc.)
  - Cuándo (timestamp)
  - Desde dónde (IP, user agent)
  - Resultado (success/failure)

**Ejemplo:**
```java
@Aspect
@Component
public class AuditAspect {
    @AfterReturning(pointcut = "@annotation(Audited)")
    public void audit(JoinPoint joinPoint) {
        // Log to audit_logs table
    }
}
```

#### 1.5 Input Sanitization

**Estado:** ✅ Validación con `@Valid`, ⚠️ Falta sanitización

**Acción requerida:**
- Sanitizar campos de texto libre (direcciones, nombres) contra XSS
- Limitar tamaño de request body para prevenir DoS
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB
server:
  tomcat:
    max-http-post-size: 10485760  # 10MB
```

---

### 2. Validación y Manejo de Errores

#### 2.1 Manejo de Errores en Producción

**Estado:** ⚠️ GlobalExceptionHandler existe pero puede exponer detalles internos

**Acción requerida:**
```java
// GlobalExceptionHandler.java - modo producción
@ExceptionHandler(Exception.class)
public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
    log.error("Unexpected error", ex);  // Log completo

    // NO exponer stack trace al cliente en producción
    if (isProd()) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            "An unexpected error occurred. Please contact support with request ID: " + requestId);
    }
    return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
        "Internal Server Error", ex.getMessage());
}
```

#### 2.2 Request Size Limits

**Acción requerida:**
```yaml
# application-prod.yml
spring:
  codec:
    max-in-memory-size: 10MB
server:
  max-http-header-size: 20KB
```

---

### 3. Base de Datos en Producción

#### 3.1 Connection Pooling (HikariCP)

**Estado:** ⚠️ Usa defaults (puede ser insuficiente bajo carga)

**Acción requerida:**
```yaml
# application-prod.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20  # Ajustar según CPU cores (2-4x cores)
      minimum-idle: 5
      connection-timeout: 30000  # 30s
      idle-timeout: 600000  # 10min
      max-lifetime: 1800000  # 30min
      leak-detection-threshold: 60000  # Detectar leaks
      pool-name: CoreBankingPool
```

#### 3.2 Índices de Base de Datos

**Estado:** ⚠️ Revisar

**Acción requerida:**
Verificar que existan índices en:
```sql
-- Verificar índices existentes
SELECT tablename, indexname, indexdef
FROM pg_indexes
WHERE schemaname = 'public';

-- Índices críticos que deberían existir:
-- users: email (único), role_id
-- customers: user_id (único), document_number (único)
-- accounts: customer_id, account_number (único), alias (único)
-- transactions: account_id, created_at, status
-- transfers: from_account_id, to_account_id, idempotency_key (único)
```

Si faltan, crear migrations adicionales.

#### 3.3 Backups Automatizados

**Estado:** ❌ No configurado

**Acción requerida:**
- **AWS RDS:** Habilitar automated backups (retention 7-30 días)
- **PostgreSQL manual:**
```bash
# Cron job diario
0 2 * * * pg_dump -U banking_user core_banking_db | gzip > /backups/db_$(date +\%Y\%m\%d).sql.gz

# Retention de 30 días
find /backups -name "db_*.sql.gz" -mtime +30 -delete
```
- **Documentar procedimiento de restore**

#### 3.4 Read Replicas (Opcional - Escalabilidad)

**Estado:** ❌ No configurado

**Para futuro:** Configurar read replicas para queries de solo lectura (reportes, listados).

---

### 4. Configuración por Ambiente

#### 4.1 Perfil de Producción Mejorado

**Estado:** ⚠️ Básico, necesita mejoras

**Acción requerida:**
```yaml
# application-prod.yml
spring:
  jpa:
    show-sql: false
    properties:
      hibernate:
        format_sql: false
        generate_statistics: false
  flyway:
    baseline-on-migrate: false
    validate-on-migrate: true
    clean-disabled: true  # CRÍTICO: prevenir borrado accidental

logging:
  level:
    root: WARN
    com.banking.system: INFO
    org.springframework.web: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"  # JSON format mejor
  file:
    name: /var/log/core-banking/application.log
    max-size: 100MB
    max-history: 30

management:
  endpoint:
    health:
      show-details: never
      show-components: never
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus  # NO exponer todo
```

#### 4.2 Logging Estructurado (JSON)

**Acción requerida:**
```xml
<!-- pom.xml -->
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>7.4</version>
</dependency>
```

```xml
<!-- logback-spring.xml -->
<configuration>
    <appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
    </appender>
    <root level="INFO">
        <appender-ref ref="JSON"/>
    </root>
</configuration>
```

---

### 5. Observabilidad y Debugging

#### 5.1 Correlation IDs (Request Tracing)

**Estado:** ❌ No implementado

**Acción requerida:**
```java
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) {
        String correlationId = request.getHeader("X-Correlation-ID");
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }

        MDC.put("correlationId", correlationId);
        response.setHeader("X-Correlation-ID", correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
```

#### 5.2 Métricas de Negocio

**Estado:** ⚠️ Solo métricas técnicas (Actuator)

**Acción requerida:**
```java
@Service
public class MetricsService {
    private final MeterRegistry meterRegistry;

    public void recordRegistration() {
        meterRegistry.counter("business.registrations.total").increment();
    }

    public void recordTransaction(TransactionType type, Money amount) {
        meterRegistry.counter("business.transactions.total",
            "type", type.name(),
            "currency", amount.currency().name()
        ).increment();

        meterRegistry.summary("business.transactions.amount",
            "currency", amount.currency().name()
        ).record(amount.amount().doubleValue());
    }
}
```

#### 5.3 Alertas

**Acción requerida:**
Configurar alertas en CloudWatch/Prometheus/Grafana para:
- Latencia P99 > 2s
- Error rate 5xx > 1%
- Database connection pool usage > 80%
- Tasa de registros fallidos > 5%
- Redis connection failures
- Disk space < 20%

#### 5.4 Dashboards

**Acción requerida:**
Crear dashboards en Grafana con:
- Request rate, latency, error rate (RED metrics)
- JVM metrics (heap, GC, threads)
- Database metrics (connections, query time)
- Business metrics (registros/hora, transacciones/min)

---

### 6. Resiliencia

#### 6.1 Circuit Breakers

**Estado:** ❌ No implementado

**Acción requerida:**
```xml
<!-- pom.xml -->
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
    <version>2.1.0</version>
</dependency>
```

```java
// Para servicios externos como Email SMTP
@Service
public class EmailService {

    @CircuitBreaker(name = "emailService", fallbackMethod = "queueEmailForRetry")
    @Retry(name = "emailService")
    public void sendEmail(String to, String subject, String body) {
        mailSender.send(message);
    }

    private void queueEmailForRetry(String to, String subject, String body, Exception e) {
        log.warn("Email service failed, queuing for retry", e);
        // Guardar en tabla email_queue para retry asíncrono
    }
}
```

```yaml
# application.yml
resilience4j:
  circuitbreaker:
    instances:
      emailService:
        registerHealthIndicator: true
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        failureRateThreshold: 50
        waitDurationInOpenState: 30s
  retry:
    instances:
      emailService:
        maxAttempts: 3
        waitDuration: 1s
        exponentialBackoffMultiplier: 2
```

#### 6.2 Timeouts

**Acción requerida:**
```yaml
# application-prod.yml
spring:
  datasource:
    hikari:
      connection-timeout: 30000
  data:
    redis:
      timeout: 5s
      lettuce:
        shutdown-timeout: 2000
  mail:
    properties:
      mail.smtp.timeout: 5000
      mail.smtp.connectiontimeout: 5000

server:
  tomcat:
    connection-timeout: 20s
```

#### 6.3 Graceful Shutdown

**Estado:** ⚠️ Spring Boot default (básico)

**Acción requerida:**
```yaml
# application-prod.yml
server:
  shutdown: graceful

spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s
```

```java
@Component
public class GracefulShutdownHook {

    @PreDestroy
    public void onShutdown() {
        log.info("Application shutting down, draining requests...");
        // Dar tiempo para que load balancer deje de enviar requests
        Thread.sleep(10000);
    }
}
```

#### 6.4 Bulkheads (Aislamiento de Recursos)

**Acción requerida (opcional):**
```java
@Bulkhead(name = "heavyOperations", type = Bulkhead.Type.THREADPOOL)
public CompletableFuture<Report> generateHeavyReport() {
    // Operación pesada que no debe saturar el thread pool principal
}
```

---

### 7. Email/Notificaciones

#### 7.1 Manejo de Fallos en Email

**Estado:** ⚠️ Falla síncronamente, puede bloquear registro

**Problema actual:**
- Si Gmail SMTP falla, el registro de usuario también falla
- No hay retry ni queue

**Acción requerida:**
```java
// Opción 1: Email asíncrono con retry
@Async
@Retry(name = "emailService")
public CompletableFuture<Void> sendEmailAsync(EmailMessage message) {
    mailSender.send(message);
    return CompletableFuture.completedFuture(null);
}

// Opción 2: Queue persistente (recomendado para producción)
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void onEmailRequested(EmailRequestedEvent event) {
    emailQueueRepository.save(new EmailQueueEntry(event));
    // Procesador asíncrono lee de la queue y reintenta
}
```

#### 7.2 Rate Limiting en Emails

**Acción requerida:**
- Limitar emails por usuario (anti-spam)
- Limitar emails totales por hora (proteger cuenta SMTP)

```java
@Service
public class EmailRateLimiter {
    private final Cache<String, AtomicInteger> emailCounts =
        Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build();

    public boolean canSendEmail(String email) {
        AtomicInteger count = emailCounts.get(email, k -> new AtomicInteger(0));
        return count.incrementAndGet() <= 5;  // Max 5 emails/hora por usuario
    }
}
```

---

### 8. CI/CD y Deployment

#### 8.1 Pipeline CI/CD

**Estado:** ❌ No configurado

**Acción requerida:**
```yaml
# .github/workflows/ci-cd.yml
name: CI/CD Pipeline

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '21'
      - name: Run unit tests
        run: mvn clean verify -Punit-tests
      - name: Run integration tests
        run: mvn verify -Pintegration-tests
      - name: Upload coverage
        uses: codecov/codecov-action@v3

  build:
    needs: test
    runs-on: ubuntu-latest
    steps:
      - name: Build Docker image
        run: docker build -t core-banking:${{ github.sha }} .
      - name: Push to registry
        run: docker push core-banking:${{ github.sha }}

  deploy-staging:
    needs: build
    if: github.ref == 'refs/heads/develop'
    runs-on: ubuntu-latest
    steps:
      - name: Deploy to staging
        run: |
          # kubectl apply / aws ecs update-service / etc.
```

#### 8.2 Health Checks para Deployment

**Estado:** ✅ Actuator health endpoint existe

**Acción requerida:** Verificar que incluya:
```java
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        // Verificar conexión a DB
        // Verificar Redis
        // Verificar espacio en disco
    }
}
```

#### 8.3 Deployment Strategy

**Acción requerida:**
- **Kubernetes:** Rolling update con readiness/liveness probes
- **AWS ECS:** Blue/Green deployment
- **Manual:** Usar script que drena conexiones antes de apagar

```yaml
# kubernetes/deployment.yaml
spec:
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  template:
    spec:
      containers:
      - name: core-banking
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 20
```

---

### 9. Documentación Operacional

#### 9.1 Runbook

**Acción requerida:** Crear `docs/RUNBOOK.md` con:
- Procedimiento de deployment
- Procedimiento de rollback
- Troubleshooting común:
  - Alta latencia en DB → revisar queries lentas
  - Redis connection errors → verificar network/credentials
  - Email failures → revisar logs, verificar SMTP quota
- Contactos de escalamiento
- SLAs/SLOs definidos

#### 9.2 Disaster Recovery Plan

**Acción requerida:**
- **RTO** (Recovery Time Objective): ¿Cuánto downtime es aceptable?
- **RPO** (Recovery Point Objective): ¿Cuánta pérdida de datos es aceptable?
- Procedimiento de restore desde backup
- Failover plan si región de AWS falla

---

## Checklist Completo

### Seguridad

- [ ] HTTPS/TLS configurado y forzado
- [ ] Secrets en vault/secrets manager (no .env en producción)
- [ ] Security headers configurados (HSTS, CSP, X-Frame-Options)
- [ ] Audit logging implementado y probado
- [ ] Input sanitization contra XSS
- [ ] Request size limits configurados
- [ ] Rate limiting probado bajo carga
- [ ] SQL injection tests ejecutados (usar OWASP ZAP)
- [ ] Dependency vulnerability scan (mvn dependency-check:check)

### Base de Datos

- [ ] Connection pool configurado (HikariCP)
- [ ] Índices optimizados y documentados
- [ ] Backup automatizado configurado
- [ ] Restore procedure documentado y probado
- [ ] Migrations tested en staging
- [ ] Query performance reviewed (explain analyze)
- [ ] Connection leak detection habilitado

### Observabilidad

- [ ] Logging estructurado (JSON format)
- [ ] Correlation IDs implementados
- [ ] Métricas de negocio configuradas
- [ ] Alertas configuradas en CloudWatch/Grafana
- [ ] Dashboards creados
- [ ] Log aggregation configurado (ELK/CloudWatch Logs)
- [ ] Distributed tracing (opcional: OpenTelemetry)

### Resiliencia

- [ ] Circuit breakers en servicios externos
- [ ] Retry policies con exponential backoff
- [ ] Timeouts configurados en todas las I/O operations
- [ ] Graceful shutdown implementado
- [ ] Bulkheads para operaciones pesadas (opcional)
- [ ] Chaos engineering básico ejecutado (opcional)

### Email/Notificaciones

- [ ] Email failures no bloquean operaciones críticas
- [ ] Queue persistente para emails con retry
- [ ] Rate limiting en envío de emails
- [ ] Circuit breaker en SMTP client
- [ ] Monitoreo de bounce rate/failures

### Testing

- [ ] 70%+ code coverage
- [ ] Integration tests pasando con TestContainers
- [ ] Load testing ejecutado (JMeter/Gatling)
- [ ] Security testing ejecutado (OWASP ZAP)
- [ ] Smoke tests en staging
- [ ] Performance baseline documentado

### Deployment

- [ ] CI/CD pipeline configurado
- [ ] Tests automatizados en pipeline
- [ ] Docker image optimizado
- [ ] Health checks configurados
- [ ] Rolling deployment strategy
- [ ] Rollback procedure documentado
- [ ] Blue/Green o Canary deployment (opcional)
- [ ] Feature flags para rollout gradual (opcional)

### Documentación

- [ ] Runbook creado
- [ ] Disaster recovery plan documentado
- [ ] Backup/restore procedure documentado
- [ ] Capacity planning ejecutado
- [ ] SLAs/SLOs definidos
- [ ] Architecture Decision Records actualizados
- [ ] API documentation actualizada (Swagger)

### Configuración

- [ ] Variables de entorno documentadas
- [ ] Secrets rotation plan definido
- [ ] Multi-environment config (dev/staging/prod)
- [ ] Resource limits definidos (CPU, memoria)
- [ ] Auto-scaling configurado (si aplica)

---

## Priorización

### 🔴 Prioridad CRÍTICA (Bloqueante para producción)

**Debe estar implementado antes de cualquier deploy a producción:**

1. **HTTPS obligatorio** con TLS 1.2+
2. **Secrets management** (Vault/AWS Secrets Manager)
3. **Connection pool** configurado correctamente
4. **Backups automatizados** de base de datos
5. **Health checks** funcionales
6. **Logging estructurado** con rotation
7. **Manejo seguro de errores** (sin exponer stack traces)
8. **Request size limits** para prevenir DoS

**Estimado de implementación:** 2-3 días

---

### 🟡 Prioridad ALTA (Primeras 2 semanas en producción)

**Implementar lo antes posible después del deploy inicial:**

9. **Circuit breakers** para servicios externos (SMTP)
10. **Correlation IDs** para tracing
11. **Métricas y alertas** básicas
12. **Audit logging** implementado
13. **CI/CD pipeline** básico
14. **Email queue** con retry
15. **Load testing** ejecutado
16. **Runbook** documentado

**Estimado de implementación:** 1-2 semanas

---

### 🟢 Prioridad MEDIA (Primer mes)

**Mejora continua y optimización:**

17. **Distributed tracing** (OpenTelemetry)
18. **Advanced dashboards** en Grafana
19. **Auto-scaling** configurado
20. **Blue/Green deployment**
21. **Performance optimization** basado en métricas
22. **Security hardening** adicional
23. **Disaster recovery testing**

**Estimado de implementación:** 2-4 semanas

---

### 🔵 Prioridad BAJA (Nice to have)

**Optimizaciones avanzadas para escalabilidad:**

24. **Chaos engineering** con Chaos Monkey
25. **Read replicas** para base de datos
26. **Caching layer** adicional (Redis cache aside)
27. **Feature flags** system
28. **Advanced security scanning** continuo
29. **Multi-region deployment**

**Estimado de implementación:** 1-3 meses

---

## Recursos y Referencias

### Herramientas Recomendadas

- **Security Testing:** OWASP ZAP, SonarQube
- **Load Testing:** JMeter, Gatling, k6
- **Monitoring:** Prometheus + Grafana, DataDog, New Relic
- **Logging:** ELK Stack (Elasticsearch + Logstash + Kibana), CloudWatch
- **CI/CD:** GitHub Actions, GitLab CI, Jenkins
- **Secrets:** AWS Secrets Manager, HashiCorp Vault
- **Tracing:** Jaeger, Zipkin, OpenTelemetry

### Documentación Oficial

- [Spring Boot Production Ready Features](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Spring Security Best Practices](https://docs.spring.io/spring-security/reference/servlet/exploits/index.html)
- [Resilience4j Documentation](https://resilience4j.readme.io/)
- [PostgreSQL Performance Tuning](https://wiki.postgresql.org/wiki/Performance_Optimization)
- [AWS Well-Architected Framework](https://aws.amazon.com/architecture/well-architected/)

### Próximos Pasos

1. Revisar este checklist con el equipo
2. Priorizar items según criticidad del negocio
3. Crear tickets/issues para cada item
4. Asignar responsables y timelines
5. Implementar en orden de prioridad
6. Testing exhaustivo en staging antes de producción
7. Deploy gradual (canary/blue-green)
8. Monitoreo intensivo post-deployment

---

**Última actualización:** 2026-02-05
**Mantenedor:** DevOps Team
**Revisión requerida:** Cada 3 meses o antes de major releases