# Resumen de Configuración del Stack de Observabilidad

## ✅ Cambios Completados

### 1. Docker Compose (`docker-compose.yml`)

**Servicios agregados:**
- ✅ **Prometheus** (puerto 9090) - Recolección de métricas
- ✅ **Loki** (puerto 3100) - Agregación de logs
- ✅ **Tempo** (puerto 3200, 9411) - Trazas distribuidas
- ✅ **Grafana** (puerto 3000) - Visualización unificada

**Variables de entorno agregadas al servicio `app`:**
```yaml
MANAGEMENT_ZIPKIN_TRACING_ENDPOINT: http://tempo:9411
LOKI_URL: http://loki:3100/loki/api/v1/push
```

**Volúmenes agregados:**
- `prometheus_data` - Persistencia de métricas
- `loki_data` - Persistencia de logs
- `tempo_data` - Persistencia de trazas
- `grafana_data` - Configuración y dashboards de Grafana

---

### 2. Logback Configuration (`src/main/resources/logback-spring.xml`)

**Cambios aplicados:**

#### Console Appender (antes):
```xml
<pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%X{correlationId:-NO-CORRELATION-ID}] %-5level %logger{36} - %msg%n</pattern>
```

#### Console Appender (después):
```xml
<pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%X{correlationId:-NO-CORRELATION-ID}] [%X{traceId:-},%X{spanId:-}] %-5level %logger{36} - %msg%n</pattern>
```

**Nuevo appender agregado:**
```xml
<appender name="LOKI" class="com.github.loki4j.logback.Loki4jAppender">
  <!-- Envía logs a Loki con formato JSON -->
  <!-- Incluye: timestamp, level, logger, message, correlationId, traceId, spanId -->
</appender>
```

**Beneficios:**
- ✅ Logs incluyen `traceId` y `spanId` automáticamente
- ✅ Logs se envían a Loki en formato JSON
- ✅ Se mantiene el `correlationId` existente
- ✅ Soporte para perfiles dev y prod

---

### 3. Application Properties (`src/main/resources/application.yml`)

**Configuración de tracing agregada:**
```yaml
management:
  tracing:
    sampling:
      probability: 1.0  # 100% sampling para dev
  zipkin:
    tracing:
      endpoint: ${MANAGEMENT_ZIPKIN_TRACING_ENDPOINT:http://localhost:9411/api/v2/spans}

loki:
  url: ${LOKI_URL:http://localhost:3100/loki/api/v1/push}
```

**Beneficios:**
- ✅ Distributed tracing habilitado
- ✅ Trazas se envían a Tempo (vía Zipkin format)
- ✅ URL de Loki configurable por variable de entorno

---

### 4. Variables de Entorno (`.env.example`)

**Variables agregadas:**
```bash
# Application port
APP_PORT=8080

# Grafana credentials
GRAFANA_USER=admin
GRAFANA_PASSWORD=admin

# Observability endpoints
LOKI_URL=http://loki:3100/loki/api/v1/push
MANAGEMENT_ZIPKIN_TRACING_ENDPOINT=http://tempo:9411
```

**Corrección aplicada:**
- `DB_USERNAME` → `DB_USER` (consistencia con docker-compose.yml)

---

### 5. Archivos de Configuración Creados

```
observability/
├── README.md                                    # Documentación completa del stack
├── prometheus/
│   └── prometheus.yml                           # Config de Prometheus
├── loki/
│   └── loki-config.yml                          # Config de Loki
├── tempo/
│   └── tempo.yml                                # Config de Tempo
└── grafana/
    ├── datasources.yml                          # Auto-provisioning de datasources
    ├── dashboards.yml                           # Auto-provisioning de dashboards
    └── dashboards/
        └── spring-boot-overview.json            # Dashboard inicial de Spring Boot
```

**Archivos eliminados:**
- ❌ `docker-config/` (carpeta antigua borrada)

---

## 🎯 Tempo vs Zipkin - Decisión

**Tu setup:**
- Dependencias: `micrometer-tracing-bridge-brave` + `zipkin-reporter-brave`
- Formato: Zipkin

**Recomendación implementada: TEMPO**

### ¿Por qué Tempo en lugar de Zipkin?

| Característica | Tempo | Zipkin |
|----------------|-------|--------|
| **Formato** | Compatible con Zipkin | Nativo |
| **Stack** | Grafana (integración nativa) | Standalone |
| **Storage** | Eficiente, bajo costo | Requiere Cassandra/Elasticsearch |
| **Recursos** | Liviano (~100MB RAM) | Pesado (~500MB+ RAM) |
| **Query** | Rápido, optimizado | Más lento |
| **Correlación** | Logs ↔ Traces ↔ Metrics | Solo traces |

**Conclusión:** Tempo es compatible con tu formato Zipkin actual, pero ofrece mejor integración con Grafana y requiere menos recursos.

---

## 📊 Flujo de Datos Completo

```
┌─────────────────────────────────────────────────────────────────┐
│                      Spring Boot Application                    │
│                                                                   │
│  Micrometer Tracing → genera traceId, spanId                    │
│  Logback → captura traceId en logs                              │
│  Actuator → expone métricas en /actuator/prometheus             │
└─────────────────────────────────────────────────────────────────┘
                            ↓ ↓ ↓
                ┌───────────┼─┼─┼───────────┐
                ↓           ↓   ↓           ↓
        ┌──────────┐  ┌────────┐  ┌──────────┐
        │Prometheus│  │  Loki  │  │  Tempo   │
        │(metrics) │  │ (logs) │  │(traces)  │
        └──────────┘  └────────┘  └──────────┘
                ↓           ↓           ↓
                └───────────┼───────────┘
                            ↓
                    ┌───────────────┐
                    │    Grafana    │
                    │               │
                    │ • Dashboards  │
                    │ • Explore     │
                    │ • Alerting    │
                    └───────────────┘
```

---

## 🚀 Cómo Usar

### 1. Copiar el archivo de entorno

```bash
cp .env.example .env
```

### 2. Configurar variables en `.env`

Mínimo requerido:
```bash
DB_USER=banking_user
DB_PASSWORD=your_password
DB_NAME=core_banking_db
JWT_SECRET=$(openssl rand -base64 32)
JWT_EXPIRATION_MS=86400000
```

### 3. Levantar el stack completo

```bash
docker-compose up -d
```

### 4. Verificar que todo esté funcionando

```bash
# Ver logs de los servicios
docker-compose logs -f app prometheus loki tempo grafana

# Verificar salud de los servicios
curl http://localhost:9090/-/healthy  # Prometheus
curl http://localhost:3100/ready      # Loki
curl http://localhost:3200/ready      # Tempo
curl http://localhost:3000/api/health # Grafana
```

### 5. Acceder a Grafana

- **URL:** http://localhost:3000
- **Usuario:** admin
- **Password:** admin

### 6. Explorar los datos

**Dashboard pre-configurado:**
1. Ir a "Dashboards" → "Core Banking"
2. Abrir "Core Banking - Spring Boot Overview"
3. Ver: Uptime, memoria, request rate, latencia, etc.

**Explorar logs:**
1. Ir a "Explore"
2. Seleccionar datasource "Loki"
3. Query: `{app="core-bank"}`
4. Click en un `traceId` → salta a la traza completa

**Explorar trazas:**
1. Ir a "Explore"
2. Seleccionar datasource "Tempo"
3. Buscar por servicio "core-bank"
4. Click en una traza → ver spans
5. Click "Logs for this span" → ver logs relacionados

---

## 🔍 Ejemplos de Queries

### PromQL (Métricas)

```promql
# Rate de requests
rate(http_server_requests_seconds_count{application="core-bank"}[1m])

# Error rate
100 * sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m]))
  / sum(rate(http_server_requests_seconds_count[5m]))

# Latencia p99
histogram_quantile(0.99,
  sum by(le) (rate(http_server_requests_seconds_bucket[5m]))
)

# Memoria JVM
jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} * 100
```

### LogQL (Logs)

```logql
# Todos los logs
{app="core-bank"}

# Solo errores
{app="core-bank"} | json | level="ERROR"

# Logs con traceId específico
{app="core-bank"} | json | traceId="abc123"

# Contar errores por logger
sum by(logger) (count_over_time({app="core-bank"} | json | level="ERROR" [1h]))
```

---

## 🎨 Personalización

### Agregar más dashboards

1. Crear archivo JSON en `observability/grafana/dashboards/`
2. Reiniciar Grafana: `docker-compose restart grafana`

### Cambiar retención de logs

Editar `observability/loki/loki-config.yml`:
```yaml
limits_config:
  retention_period: 336h  # 14 días
```

### Ajustar sampling en producción

En `application.yml` o `.env`:
```yaml
management:
  tracing:
    sampling:
      probability: 0.1  # Solo 10% de requests
```

---

## 📝 Próximos Pasos (Opcionales)

1. **Alertas:** Configurar alertas en Prometheus para errores, latencia, etc.
2. **Exporters:** Agregar PostgreSQL exporter para métricas de base de datos
3. **Service Mesh:** Considerar Istio/Linkerd para observabilidad automática
4. **APM:** Integrar con Elastic APM o New Relic para profiling detallado
5. **Dashboards:** Crear dashboards específicos por dominio (Auth, Customers, etc.)

---

## 🐛 Troubleshooting

### No veo métricas en Prometheus

```bash
# Verificar que el actuator esté expuesto
curl http://localhost:8080/actuator/prometheus

# Ver targets en Prometheus
# http://localhost:9090/targets
# Debe aparecer "spring-boot-app" con estado UP
```

### No veo logs en Loki

```bash
# Ver logs del appender
docker-compose logs app | grep -i loki

# Verificar que Loki esté recibiendo datos
curl http://localhost:3100/loki/api/v1/labels
```

### No veo trazas en Tempo

```bash
# Verificar config de tracing
curl http://localhost:8080/actuator/configprops | grep -i tracing

# Ver si Tempo está recibiendo datos
docker-compose logs tempo | grep -i span
```

---

## 📚 Documentación Completa

Ver `observability/README.md` para:
- Guía completa de configuración
- Queries avanzadas
- Best practices
- Troubleshooting detallado

---

## ✨ Resumen de Beneficios

✅ **Métricas en tiempo real:** CPU, memoria, requests/sec, errores, latencia
✅ **Logs centralizados:** Búsqueda potente con filtros por nivel, traceId, etc.
✅ **Trazas distribuidas:** Ver el flujo completo de cada request
✅ **Correlación automática:** Click en traceId → ver logs y traces relacionados
✅ **Dashboards pre-configurados:** Visualización inmediata sin configuración manual
✅ **Auto-provisioning:** Datasources y dashboards versionados en Git
✅ **Stack unificado:** Una sola herramienta (Grafana) para todo
✅ **Open source:** Sin costos de licencias
✅ **Producción-ready:** Usado por empresas como GitLab, Grafana Labs, etc.

---

**Creado:** 2026-02-13
**Stack:** Prometheus + Loki + Tempo + Grafana (LGTM)
**Spring Boot:** 3.5.9
**Java:** 21