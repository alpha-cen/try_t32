# Observability Guide - Auth User Service

Complete guide to monitoring, metrics, logging, and tracing for the Auth User Service.

## 🎯 Overview

The Auth User Service includes comprehensive observability features:

- **📊 Metrics** - Prometheus metrics with custom auth metrics
- **📝 Logging** - Structured JSON logging with Logback
- **🔍 Tracing** - Distributed tracing with OpenTelemetry
- **📈 Dashboards** - Pre-built Grafana dashboards
- **🏥 Health Checks** - Kubernetes-ready health probes

---

## 🚀 Quick Start

### Start with Monitoring Stack

```bash
# Start all services including Prometheus and Grafana
docker-compose up -d

# Verify services are running
docker-compose ps
```

### Access Dashboards

| Service | URL | Credentials |
|---------|-----|-------------|
| **Grafana** | http://localhost:3000 | admin / admin |
| **Prometheus** | http://localhost:9090 | - |
| **Application** | http://localhost:8080 | - |
| **Metrics Endpoint** | http://localhost:8080/actuator/prometheus | - |

---

## 📊 Metrics

### Built-in Metrics

The service exposes numerous metrics automatically:

#### JVM Metrics
- `jvm_memory_used_bytes` - JVM memory usage
- `jvm_memory_max_bytes` - JVM memory limits
- `jvm_gc_pause_seconds` - Garbage collection pauses
- `jvm_threads_live` - Active thread count

#### HTTP Metrics
- `http_server_requests_seconds` - HTTP request duration
- `http_server_requests_seconds_count` - HTTP request count
- `http_server_requests_seconds_sum` - Total HTTP request time

#### Database Metrics
- `hikaricp_connections` - Total connections
- `hikaricp_connections_active` - Active connections
- `hikaricp_connections_idle` - Idle connections
- `hikaricp_connections_pending` - Pending connections

#### System Metrics
- `system_cpu_usage` - System CPU usage
- `process_cpu_usage` - Process CPU usage
- `process_uptime_seconds` - Application uptime

### Custom Authentication Metrics

#### Counters
- `auth_login_success_total` - Successful login attempts
- `auth_login_failure_total` - Failed login attempts
- `auth_registration_success_total` - Successful registrations
- `auth_registration_failure_total` - Failed registrations
- `auth_password_reset_total` - Password reset requests
- `auth_password_change_total` - Password change requests
- `auth_token_refresh_total` - Token refresh requests

#### Timers
- `auth_login_duration_seconds` - Login duration (histogram)
- `auth_registration_duration_seconds` - Registration duration (histogram)

### Query Metrics

#### Prometheus Queries

**Login Success Rate:**
```promql
sum(rate(auth_login_success_total[5m])) / 
(sum(rate(auth_login_success_total[5m])) + sum(rate(auth_login_failure_total[5m]))) * 100
```

**Login Requests Per Second:**
```promql
sum(rate(auth_login_success_total[1m])) + sum(rate(auth_login_failure_total[1m]))
```

**Login Duration Percentiles:**
```promql
# p50
histogram_quantile(0.50, sum(rate(auth_login_duration_seconds_bucket[5m])) by (le))

# p95
histogram_quantile(0.95, sum(rate(auth_login_duration_seconds_bucket[5m])) by (le))

# p99
histogram_quantile(0.99, sum(rate(auth_login_duration_seconds_bucket[5m])) by (le))
```

**HTTP Error Rate:**
```promql
sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) /
sum(rate(http_server_requests_seconds_count[5m])) * 100
```

---

## 📝 Logging

### Structured Logging

The service uses **Logback** with **Logstash encoder** for structured JSON logging.

#### Log Formats

**Development (Console):**
```
2026-02-06 10:30:15.123 [http-nio-8080-exec-1] INFO  c.a.controller.AuthController - Login attempt for user: john@example.com
```

**Production (JSON):**
```json
{
  "timestamp": "2026-02-06T10:30:15.123Z",
  "level": "INFO",
  "thread": "http-nio-8080-exec-1",
  "logger": "com.authservice.controller.AuthController",
  "message": "Login attempt for user: john@example.com",
  "application": "auth-user-service",
  "environment": "docker",
  "trace_id": "abc123",
  "span_id": "def456"
}
```

#### Log Levels

Configure via environment variable:
```bash
export SPRING_PROFILES_ACTIVE=prod  # JSON logging
export SPRING_PROFILES_ACTIVE=dev   # Console logging
```

#### Key Log Events

| Event | Level | Message Pattern |
|-------|-------|-----------------|
| Login Success | INFO | "Login successful for user: {username} (duration: {ms}ms)" |
| Login Failure | ERROR | "Login failed for user: {username} (duration: {ms}ms)" |
| Registration | INFO | "Registration successful for user: {username}" |
| Password Reset | INFO | "Password reset request for user: {username}" |
| Token Refresh | INFO | "Token refresh request" |

#### View Logs

```bash
# Container logs
docker logs -f auth-service

# Follow last 100 lines
docker logs --tail=100 -f auth-service

# JSON logs in container
docker exec auth-service cat /tmp/auth-service.log.json
```

### Log Aggregation

#### ELK Stack Integration

The JSON logs are compatible with Elasticsearch/Logstash/Kibana:

```yaml
# Filebeat configuration example
filebeat.inputs:
  - type: container
    paths:
      - '/var/lib/docker/containers/*/*.log'
    processors:
      - add_docker_metadata: ~
      - decode_json_fields:
          fields: ["message"]
          target: ""

output.elasticsearch:
  hosts: ["elasticsearch:9200"]
```

---

## 🔍 Distributed Tracing

### OpenTelemetry Integration

The service includes OpenTelemetry for distributed tracing.

#### Trace Context

Every request includes:
- **Trace ID** - Unique identifier for the entire request flow
- **Span ID** - Unique identifier for this service's operation
- **Parent Span ID** - Link to calling service

#### Viewing Traces

**With Jaeger:**
```yaml
# docker-compose addition
jaeger:
  image: jaegertracing/all-in-one:latest
  ports:
    - "16686:16686"  # UI
    - "4318:4318"    # OTLP HTTP receiver
  environment:
    - COLLECTOR_OTLP_ENABLED=true
```

Access Jaeger UI: http://localhost:16686

#### Trace Examples

**Login Request Trace:**
```
Trace ID: abc123def456
├── HTTP POST /api/auth/login (200ms)
    ├── CognitoService.authenticateUser (150ms)
    │   └── AWS Cognito API Call (130ms)
    └── UserService.findByUsername (30ms)
        └── Database Query (25ms)
```

---

## 📈 Grafana Dashboards

### Pre-built Dashboard

The service includes a comprehensive Grafana dashboard with:

#### Panels

1. **Login Success Rate** - Percentage of successful logins
2. **Registration Success Rate** - Percentage of successful registrations
3. **Login Attempts Graph** - Success vs failure over time
4. **Registration Attempts Graph** - Success vs failure over time
5. **Login Duration Percentiles** - p50, p95, p99 latencies
6. **Token Refresh Rate** - Tokens refreshed per second
7. **Password Operations** - Reset and change operations
8. **JVM Memory Usage** - Heap memory utilization
9. **HTTP Request Rate** - Requests per endpoint
10. **Database Connection Pool** - Active, idle, and total connections
11. **System CPU Usage** - Overall system CPU
12. **Process CPU Usage** - Application CPU usage

### Access Dashboard

1. Open Grafana: http://localhost:3000
2. Login: admin / admin
3. Navigate to **Dashboards** → **Auth User Service - Observability Dashboard**

### Custom Dashboard

Create custom dashboards:

```json
{
  "dashboard": {
    "title": "My Custom Dashboard",
    "panels": [
      {
        "title": "Login Rate",
        "targets": [
          {
            "expr": "sum(rate(auth_login_success_total[1m]))"
          }
        ]
      }
    ]
  }
}
```

---

## 🏥 Health Checks

### Endpoints

| Endpoint | Purpose | Response |
|----------|---------|----------|
| `/actuator/health` | Overall health | `{"status":"UP"}` |
| `/actuator/health/liveness` | Kubernetes liveness probe | `{"status":"UP"}` |
| `/actuator/health/readiness` | Kubernetes readiness probe | `{"status":"UP"}` |

### Health Indicators

The application checks:
- ✅ Database connectivity
- ✅ Disk space
- ✅ AWS Cognito connectivity (via custom indicator if needed)

### Kubernetes Probes

```yaml
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: auth-service
    livenessProbe:
      httpGet:
        path: /actuator/health/liveness
        port: 8080
      initialDelaySeconds: 60
      periodSeconds: 10
    readinessProbe:
      httpGet:
        path: /actuator/health/readiness
        port: 8080
      initialDelaySeconds: 30
      periodSeconds: 5
```

---

## 🎨 Alerting

### Prometheus Alerts

Create alert rules in `prometheus.yml`:

```yaml
groups:
  - name: auth-service-alerts
    rules:
      - alert: HighLoginFailureRate
        expr: |
          sum(rate(auth_login_failure_total[5m])) / 
          (sum(rate(auth_login_success_total[5m])) + sum(rate(auth_login_failure_total[5m]))) > 0.3
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High login failure rate"
          description: "Login failure rate is above 30% for 5 minutes"

      - alert: HighAPILatency
        expr: histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket[5m])) by (le, uri)) > 1
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High API latency"
          description: "95th percentile latency is above 1 second"

      - alert: ServiceDown
        expr: up{job="auth-user-service"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Auth service is down"
          description: "Auth service has been down for 1 minute"

      - alert: HighMemoryUsage
        expr: jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} > 0.9
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High memory usage"
          description: "JVM heap usage is above 90%"

      - alert: DatabaseConnectionPoolExhausted
        expr: hikaricp_connections_active >= hikaricp_connections_max
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "Database connection pool exhausted"
          description: "All database connections are in use"
```

### Grafana Alerting

Configure alerts in Grafana:

1. Open panel → Edit
2. Click **Alert** tab
3. Configure conditions
4. Add notification channels (Slack, Email, PagerDuty, etc.)

---

## 📊 Monitoring Best Practices

### 1. Key Metrics to Monitor

**Golden Signals:**
- **Latency** - Request duration (p50, p95, p99)
- **Traffic** - Requests per second
- **Errors** - Error rate percentage
- **Saturation** - CPU, memory, database connections

**Authentication Specific:**
- Login success/failure rate
- Registration rate
- Token refresh rate
- Password reset frequency

### 2. Alert Thresholds

| Metric | Warning | Critical |
|--------|---------|----------|
| Login Failure Rate | > 20% | > 40% |
| P95 Latency | > 500ms | > 1s |
| Error Rate | > 1% | > 5% |
| Memory Usage | > 80% | > 90% |
| CPU Usage | > 70% | > 85% |

### 3. Dashboard Organization

**Overview Dashboard:**
- Service health status
- Request rate
- Error rate
- Latency percentiles

**Authentication Dashboard:**
- Login/registration metrics
- User operations
- Token management

**Infrastructure Dashboard:**
- CPU, memory, disk
- Database connections
- JVM metrics

### 4. Log Levels

**Production:**
```yaml
logging.level:
  root: INFO
  com.authservice: INFO
  org.springframework.security: WARN
```

**Development:**
```yaml
logging.level:
  root: INFO
  com.authservice: DEBUG
  org.springframework.security: DEBUG
```

---

## 🔧 Troubleshooting

### High Memory Usage

```bash
# Check JVM memory
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# Check heap dump
docker exec auth-service jmap -dump:live,format=b,file=/tmp/heapdump.hprof 1

# Copy heap dump
docker cp auth-service:/tmp/heapdump.hprof ./
```

### Slow Queries

```bash
# Enable SQL logging
export LOGGING_LEVEL_ORG_HIBERNATE_SQL=DEBUG
export LOGGING_LEVEL_ORG_HIBERNATE_TYPE_DESCRIPTOR_SQL_BASICBINDER=TRACE

# Check slow query log
docker logs auth-service | grep "SELECT" | grep -v "TRACE"
```

### High Error Rate

```bash
# Check error logs
docker logs auth-service | grep ERROR

# Check error metrics
curl http://localhost:8080/actuator/metrics/http.server.requests | jq '.measurements[] | select(.statistic=="COUNT" and .value > 0)'
```

---

## 📚 Additional Resources

### Prometheus
- [Prometheus Query Language](https://prometheus.io/docs/prometheus/latest/querying/basics/)
- [Best Practices](https://prometheus.io/docs/practices/naming/)

### Grafana
- [Dashboard Best Practices](https://grafana.com/docs/grafana/latest/best-practices/)
- [Panel Types](https://grafana.com/docs/grafana/latest/panels/)

### OpenTelemetry
- [OpenTelemetry Java](https://opentelemetry.io/docs/instrumentation/java/)
- [Tracing Best Practices](https://opentelemetry.io/docs/concepts/signals/traces/)

### Logback
- [Logback Configuration](https://logback.qos.ch/manual/configuration.html)
- [Structured Logging](https://github.com/logfellow/logstash-logback-encoder)

---

## ✅ Observability Checklist

### Development
- [ ] Metrics endpoint accessible
- [ ] Logs are readable in console
- [ ] Health checks respond correctly
- [ ] Custom metrics are being recorded

### Staging
- [ ] Grafana dashboards configured
- [ ] Prometheus scraping metrics
- [ ] Alerts configured
- [ ] Log aggregation working

### Production
- [ ] JSON logging enabled
- [ ] Traces being collected
- [ ] Alert notifications configured
- [ ] Dashboards shared with team
- [ ] Runbooks created for alerts
- [ ] Backup monitoring in place

---

**Status:** ✅ Fully Configured  
**Dashboard:** http://localhost:3000  
**Metrics:** http://localhost:8080/actuator/prometheus  
**Prometheus:** http://localhost:9090
