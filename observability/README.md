# Observability Stack - Core Banking System

Complete observability setup using Grafana LGTM stack (Loki, Grafana, Tempo, Mimir/Prometheus).

## Stack Components

| Component | Purpose | Port | URL |
|-----------|---------|------|-----|
| **Prometheus** | Metrics storage & querying | 9090 | http://localhost:9090 |
| **Loki** | Log aggregation | 3100 | http://localhost:3100 |
| **Tempo** | Distributed tracing | 3200, 9411 | http://localhost:3200 |
| **Grafana** | Visualization & dashboards | 3000 | http://localhost:3000 |

## Architecture

```
Spring Boot App
    ↓
    ├─ Metrics (Prometheus format) ──→ Prometheus ──┐
    ├─ Logs (JSON + Loki appender) ──→ Loki ────────┤
    └─ Traces (Zipkin format) ────────→ Tempo ───────┤
                                                      ↓
                                                  Grafana
                                                (Unified View)
```

## Quick Start

### 1. Start the stack

```bash
docker-compose up -d
```

### 2. Access Grafana

- URL: http://localhost:3000
- Username: `admin` (or value from `GRAFANA_USER` env var)
- Password: `admin` (or value from `GRAFANA_PASSWORD` env var)

### 3. Explore your data

**Dashboards:**
- Navigate to "Dashboards" → "Core Banking" folder
- Pre-configured dashboard: "Core Banking - Spring Boot Overview"

**Explore metrics:**
- Go to "Explore" → Select "Prometheus" datasource
- Query examples:
  ```promql
  # Request rate
  rate(http_server_requests_seconds_count{application="core-bank"}[1m])

  # Error rate
  rate(http_server_requests_seconds_count{application="core-bank",status="500"}[1m])

  # JVM memory
  jvm_memory_used_bytes{application="core-bank",area="heap"}
  ```

**Explore logs:**
- Go to "Explore" → Select "Loki" datasource
- Query examples:
  ```logql
  # All logs from the app
  {app="core-bank"}

  # Filter by log level
  {app="core-bank"} |= "ERROR"

  # Filter by trace ID
  {app="core-bank"} | json | traceId="your-trace-id"
  ```

**Explore traces:**
- Go to "Explore" → Select "Tempo" datasource
- Search by:
  - Trace ID (from logs)
  - Service name: `core-bank`
  - Time range

## Data Correlation

### Logs → Traces
1. In Loki, find a log with a `traceId`
2. Click on the trace ID link
3. Opens the full trace in Tempo

### Traces → Logs
1. In Tempo, click on any span
2. Click "Logs for this span"
3. Shows related logs in Loki

### Metrics → Traces
1. In Prometheus, view a metric
2. Click on an exemplar point
3. Opens the related trace

## Configuration Files

### Prometheus (`prometheus/prometheus.yml`)
- Scrape interval: 15s
- Targets: Spring Boot app (`app:8080/actuator/prometheus`)
- Self-monitoring enabled

### Loki (`loki/loki-config.yml`)
- Storage: Local filesystem
- Retention: 7 days (168h)
- Schema: v13 with TSDB

### Tempo (`tempo/tempo.yml`)
- Receivers: Zipkin (9411), OTLP (4317/4318)
- Retention: 7 days
- Metrics generator: Enabled (service graphs, span metrics)
- Writes metrics back to Prometheus

### Grafana
- **Datasources** (`grafana/datasources.yml`): Auto-provisioned
  - Prometheus (default)
  - Loki (with trace correlation)
  - Tempo (with log/metric correlation)
- **Dashboards** (`grafana/dashboards.yml`): Auto-loaded from `dashboards/`

## Application Configuration

### Environment Variables (set in docker-compose.yml)

```yaml
MANAGEMENT_ZIPKIN_TRACING_ENDPOINT: http://tempo:9411
LOKI_URL: http://loki:3100/loki/api/v1/push
```

### Spring Boot Properties (application.yml)

```yaml
management:
  tracing:
    sampling:
      probability: 1.0  # 100% sampling (adjust to 0.1 in production)
  zipkin:
    tracing:
      endpoint: ${MANAGEMENT_ZIPKIN_TRACING_ENDPOINT}

loki:
  url: ${LOKI_URL}
```

### Logback Configuration (logback-spring.xml)

- Console appender: Human-readable with trace IDs
- Loki appender: JSON format with labels
- Fields: `timestamp`, `level`, `logger`, `message`, `correlationId`, `traceId`, `spanId`

## Key Features

### 1. Distributed Tracing
- Every request gets a unique `traceId` and `spanId`
- Automatically propagated across services
- Visible in logs, metrics, and traces

### 2. Correlation IDs
- Custom `correlationId` preserved (from your existing implementation)
- Useful for business-level request tracking
- Separate from technical `traceId`

### 3. Automatic Service Discovery
- Prometheus automatically scrapes Spring Boot actuator
- No manual registration needed

### 4. Retention Policies
- Logs: 7 days
- Traces: 7 days
- Metrics: Prometheus default (15 days)

## Customization

### Add Custom Dashboards

1. Create a new JSON file in `observability/grafana/dashboards/`
2. Restart Grafana or wait 30s for auto-reload
3. Dashboard appears in "Core Banking" folder

### Adjust Retention

**Loki** (`loki/loki-config.yml`):
```yaml
limits_config:
  retention_period: 336h  # 14 days
```

**Tempo** (`tempo/tempo.yml`):
```yaml
compactor:
  compaction:
    block_retention: 336h  # 14 days
```

### Add Alerts

Create `prometheus/alerts/banking-alerts.yml`:
```yaml
groups:
  - name: banking
    rules:
      - alert: HighErrorRate
        expr: rate(http_server_requests_seconds_count{status="500"}[1m]) > 0.1
        for: 5m
        annotations:
          summary: "High error rate detected"
```

Then uncomment in `prometheus/prometheus.yml`:
```yaml
rule_files:
  - "alerts/*.yml"
```

## Monitoring Best Practices

### 1. Sampling in Production
- Set `management.tracing.sampling.probability: 0.1` (10%)
- Reduces overhead while capturing enough traces

### 2. Log Levels
- Development: `INFO` for app, `DEBUG` for SQL
- Production: `WARN` for root, `INFO` for app

### 3. Metric Labels
- Keep cardinality low (avoid unique IDs in labels)
- Use exemplars to link high-cardinality data to traces

### 4. Dashboard Organization
- One overview dashboard (performance, errors, resources)
- Detailed dashboards per domain (auth, customers, transactions)

## Troubleshooting

### Application not appearing in Prometheus
1. Check app is running: `docker ps`
2. Test actuator endpoint: `curl http://localhost:8080/actuator/prometheus`
3. Check Prometheus targets: http://localhost:9090/targets

### No logs in Loki
1. Check Loki is running: `docker logs core-banking-loki`
2. Test Loki API: `curl http://localhost:3100/ready`
3. Check logback config has LOKI appender
4. Verify `LOKI_URL` environment variable

### No traces in Tempo
1. Check Tempo is running: `docker logs core-banking-tempo`
2. Test Tempo API: `curl http://localhost:3200/ready`
3. Verify `MANAGEMENT_ZIPKIN_TRACING_ENDPOINT` is set
4. Check trace sampling is enabled (probability > 0)

### Grafana datasources not working
1. Check datasource health in Grafana: Configuration → Data sources
2. Verify service names match (prometheus, loki, tempo)
3. Check Docker network: `docker network inspect banking-network`

## Useful Queries

### PromQL (Prometheus)

```promql
# Request rate by endpoint
sum by(uri) (rate(http_server_requests_seconds_count{application="core-bank"}[5m]))

# 99th percentile latency
histogram_quantile(0.99, sum by(uri, le) (rate(http_server_requests_seconds_bucket{application="core-bank"}[5m])))

# Error rate percentage
100 * sum(rate(http_server_requests_seconds_count{application="core-bank",status=~"5.."}[5m]))
  / sum(rate(http_server_requests_seconds_count{application="core-bank"}[5m]))

# JVM heap usage percentage
100 * jvm_memory_used_bytes{application="core-bank",area="heap"}
  / jvm_memory_max_bytes{application="core-bank",area="heap"}
```

### LogQL (Loki)

```logql
# Errors in the last hour
{app="core-bank"} | json | level="ERROR"

# Logs for a specific trace
{app="core-bank"} | json | traceId="abc123def456"

# Count errors by logger
sum by(logger) (count_over_time({app="core-bank"} | json | level="ERROR" [1h]))

# Slow requests (> 1s latency)
{app="core-bank"} |= "duration" | json | duration > 1000
```

## Resources

- [Prometheus Documentation](https://prometheus.io/docs/)
- [Loki Documentation](https://grafana.com/docs/loki/latest/)
- [Tempo Documentation](https://grafana.com/docs/tempo/latest/)
- [Grafana Documentation](https://grafana.com/docs/grafana/latest/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer Tracing](https://micrometer.io/docs/tracing)