# GetHome Application Monitoring

This document describes the monitoring setup for the GetHome application using Prometheus, Grafana, and AlertManager.

## Overview

The monitoring stack consists of:

- **Prometheus**: Metrics collection and storage
- **Grafana**: Metrics visualization and dashboards
- **AlertManager**: Alert routing and notification
- **Custom Metrics**: Application-specific metrics from all services

## Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Application   │    │   Prometheus    │    │     Grafana     │
│     Services    │───▶│   (Metrics      │───▶│   (Dashboards   │
│                 │    │   Collection)   │    │   & Alerts)     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                                ▼
                       ┌─────────────────┐
                       │  AlertManager   │
                       │  (Notifications)│
                       └─────────────────┘
                                │
                                ▼
                       ┌─────────────────┐
                       │   Slack/Email   │
                       │   (Alerts)      │
                       └─────────────────┘
```

## Components

### 1. Prometheus

**Purpose**: Metrics collection, storage, and alerting

**Configuration**:
- Scrapes metrics from all services every 15 seconds
- Stores data for 15 days
- Uses 50GB persistent storage
- Monitors:
  - All microservices (auth, user management, message, routing, AI)
  - MongoDB database
  - Kubernetes infrastructure
  - Application-specific metrics

**Access**: `https://gethome.local/prometheus`

### 2. Grafana

**Purpose**: Metrics visualization and dashboards

**Configuration**:
- Pre-configured dashboards for GetHome application
- Prometheus as default data source
- Admin credentials: `admin/admin123`
- Uses 10GB persistent storage

**Dashboards**:
1. **GetHome Overview**: High-level application health
2. **GetHome Services**: Detailed service metrics
3. **GetHome Infrastructure**: Kubernetes and infrastructure metrics

**Access**: `https://gethome.local/monitoring`

### 3. AlertManager

**Purpose**: Alert routing and notification management

**Configuration**:
- Routes alerts to Slack channels
- Groups related alerts
- Implements alert silencing and inhibition
- Uses 5GB persistent storage

**Channels**:
- `#gethome-alerts`: General alerts
- `#gethome-critical`: Critical alerts

## Metrics

### Application Metrics

#### Auth Service
- `auth_login_attempts_total`: Total login attempts
- `auth_login_failures_total`: Failed login attempts
- `auth_registrations_total`: User registrations
- `http_requests_total`: HTTP request count
- `http_request_duration_seconds`: Request duration

#### User Management Service
- `user_profile_updates_total`: Profile update count
- `user_emergency_contacts_total`: Emergency contact operations
- `http_requests_total`: HTTP request count
- `http_request_duration_seconds`: Request duration

#### Emergency Service
- `emergency_alerts_total`: Emergency alert count
- `emergency_response_time_seconds`: Response time
- `http_requests_total`: HTTP request count
- `http_request_duration_seconds`: Request duration

#### AI Service
- `ai_requests_total`: AI request count
- `ai_request_duration_seconds`: AI processing time
- `ai_model_accuracy`: Model accuracy metrics
- `http_requests_total`: HTTP request count

#### Routing Service
- `routing_requests_total`: Routing request count
- `routing_duration_seconds`: Route calculation time
- `http_requests_total`: HTTP request count
- `http_request_duration_seconds`: Request duration

### Infrastructure Metrics

#### Kubernetes
- `kube_pod_status_phase`: Pod status
- `kube_pod_container_status_restarts_total`: Pod restarts
- `kube_node_status_condition`: Node health

#### System
- `node_cpu_seconds_total`: CPU usage
- `node_memory_MemTotal_bytes`: Memory usage
- `node_filesystem_avail_bytes`: Disk space

#### MongoDB
- `mongodb_connections`: Active connections
- `mongodb_operations_total`: Database operations
- `mongodb_query_duration_seconds`: Query performance

## Alerts

### Service Alerts

#### Critical Alerts
- **ServiceDown**: Service is down for >1 minute
- **MessageServiceUnavailable**: Message service down for >30 seconds
- **EndToEndHealthCheckFailed**: Frontend unavailable for >2 minutes
- **APIHealthCheckFailed**: Core API services down for >1 minute

#### Warning Alerts
- **HighErrorRate**: Error rate >10% for >2 minutes
- **HighResponseTime**: 95th percentile response time >2 seconds
- **HighMemoryUsage**: Memory usage >1GB
- **HighCPUUsage**: CPU usage >80%

### Infrastructure Alerts

#### Warning Alerts
- **PodRestartingFrequently**: Pod restarted >5 times in 1 hour
- **NodeHighCPUUsage**: Node CPU usage >80%
- **NodeHighMemoryUsage**: Node memory usage >85%
- **DiskSpaceLow**: Disk space <10%

### Business Alerts

#### Warning Alerts
- **HighLoginFailureRate**: Login failures >0.1/sec
- **AIServiceSlow**: AI service response time >5 seconds
- **DatabaseConnectionIssues**: MongoDB connections >100

## Setup Instructions

### 1. Enable Monitoring

Update your values file to enable monitoring:

```yaml
monitoring:
  enabled: true
  prometheus:
    enabled: true
  grafana:
    enabled: true
  alertmanager:
    enabled: true
```

### 2. Configure Slack Notifications

Update the AlertManager configuration:

```yaml
monitoring:
  alertmanager:
    slack:
      webhookUrl: "https://hooks.slack.com/services/YOUR/SLACK/WEBHOOK"
      channel: "#gethome-alerts"
      criticalChannel: "#gethome-critical"
```

### 3. Deploy with Monitoring

```bash
helm upgrade --install gethome-app ./helm/gethome-app \
  --namespace devops25-k8s-gethome \
  --set monitoring.enabled=true
```

### 4. Access Monitoring Tools

- **Grafana**: `https://gethome.local/monitoring`
  - Username: `admin`
  - Password: `admin123`

- **Prometheus**: `https://gethome.local/prometheus`

## Dashboard Guide

### GetHome Overview Dashboard

**Panels**:
1. **Service Health**: Shows up/down status of all services
2. **HTTP Request Rate**: Request rate per service
3. **Response Time**: 95th percentile response times
4. **Error Rate**: Error rates per service
5. **Memory Usage**: Memory consumption per service
6. **CPU Usage**: CPU usage per service

### GetHome Services Dashboard

**Panels**:
1. **Auth Service Metrics**:
   - Login attempts per second
   - Authentication success rate
   - JWT token operations

2. **User Management Metrics**:
   - Profile updates per second
   - Emergency contact operations
   - User creation rate

3. **AI Service Metrics**:
   - AI requests per second
   - Model processing time
   - Model accuracy

4. **Message Service Metrics**:
   - Emergency alerts per second
   - Response time
   - Alert resolution time

### GetHome Infrastructure Dashboard

**Panels**:
1. **Kubernetes Pod Status**: Pod health across namespace
2. **Node CPU Usage**: CPU usage per node
3. **Node Memory Usage**: Memory usage per node
4. **MongoDB Connections**: Database connection count

## Custom Metrics

### Adding Custom Metrics

#### Spring Boot Services

Add to your Spring Boot application:

```java
@RestController
public class MetricsController {
    
    private final Counter loginAttempts = Counter.builder("auth_login_attempts_total")
        .description("Total login attempts")
        .register(Metrics.globalRegistry);
    
    private final Timer requestTimer = Timer.builder("http_request_duration_seconds")
        .description("HTTP request duration")
        .register(Metrics.globalRegistry);
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        loginAttempts.increment();
        
        Timer.Sample sample = Timer.start();
        try {
            // Login logic
            return ResponseEntity.ok().build();
        } finally {
            sample.stop(requestTimer);
        }
    }
}
```

#### Python AI Service

Add to your Python service:

```python
from prometheus_client import Counter, Histogram, generate_latest

# Define metrics
ai_requests_total = Counter('ai_requests_total', 'Total AI requests')
ai_request_duration = Histogram('ai_request_duration_seconds', 'AI request duration')

@app.route('/metrics')
def metrics():
    return generate_latest()

@app.route('/ai/process')
def process_ai_request():
    ai_requests_total.inc()
    
    with ai_request_duration.time():
        # AI processing logic
        pass
```

#### React Client

Add to your React application:

```javascript
// Custom metrics for frontend
const trackEvent = (eventName, value = 1) => {
  if (window.gtag) {
    window.gtag('event', eventName, {
      value: value
    });
  }
};

// Track page views, user interactions, etc.
```

## Troubleshooting

### Common Issues

#### Prometheus Not Scraping Metrics

1. Check service endpoints:
   ```bash
   kubectl get endpoints -n devops25-k8s-gethome
   ```

2. Verify metrics endpoints:
   ```bash
   curl http://auth-service:8080/actuator/prometheus
   ```

3. Check Prometheus targets:
   - Go to `https://gethome.local/prometheus/targets`
   - Look for failed targets

#### Grafana Not Loading Dashboards

1. Check Grafana logs:
   ```bash
   kubectl logs -f deployment/grafana -n devops25-k8s-gethome
   ```

2. Verify data source connection:
   - Go to Grafana → Configuration → Data Sources
   - Test Prometheus connection

#### Alerts Not Firing

1. Check AlertManager configuration:
   ```bash
   kubectl get configmap alertmanager-config -n devops25-k8s-gethome -o yaml
   ```

2. Verify Slack webhook URL
3. Check AlertManager logs:
   ```bash
   kubectl logs -f deployment/alertmanager -n devops25-k8s-gethome
   ```

### Debug Commands

```bash
# Check monitoring pods
kubectl get pods -n devops25-k8s-gethome -l app=prometheus
kubectl get pods -n devops25-k8s-gethome -l app=grafana
kubectl get pods -n devops25-k8s-gethome -l app=alertmanager

# Check monitoring services
kubectl get services -n devops25-k8s-gethome | grep -E "(prometheus|grafana|alertmanager)"

# Check monitoring storage
kubectl get pvc -n devops25-k8s-gethome | grep -E "(prometheus|grafana|alertmanager)"

# Port forward for local access
kubectl port-forward svc/grafana 3000:3000 -n devops25-k8s-gethome
kubectl port-forward svc/prometheus 9090:9090 -n devops25-k8s-gethome
```

## Best Practices

### Monitoring Best Practices

1. **Use Meaningful Metric Names**: Follow Prometheus naming conventions
2. **Add Labels Carefully**: Use labels for dimensions, not cardinality
3. **Set Appropriate Alert Thresholds**: Avoid alert fatigue
4. **Monitor Business Metrics**: Track user-facing metrics
5. **Regular Dashboard Reviews**: Keep dashboards relevant

### Alert Best Practices

1. **Use Appropriate Severity Levels**: Critical vs Warning
2. **Group Related Alerts**: Reduce notification noise
3. **Set Reasonable Timeouts**: Avoid false positives
4. **Document Alert Procedures**: Include runbooks
5. **Regular Alert Testing**: Verify alert delivery

### Performance Best Practices

1. **Optimize Scrape Intervals**: Balance freshness vs load
2. **Use Recording Rules**: Pre-compute expensive queries
3. **Monitor Prometheus Itself**: Watch for cardinality issues
4. **Regular Data Retention**: Clean up old metrics
5. **Resource Limits**: Set appropriate CPU/memory limits

## Scaling Monitoring

### Horizontal Scaling

For high-volume environments:

1. **Prometheus Federation**: Use multiple Prometheus instances
2. **Grafana Clustering**: Deploy multiple Grafana instances
3. **AlertManager Clustering**: High availability for alerts

### Storage Scaling

1. **Remote Storage**: Use Thanos or Cortex for long-term storage
2. **Data Retention**: Adjust retention periods based on needs
3. **Storage Classes**: Use appropriate storage classes for performance

## Security Considerations

1. **Authentication**: Secure Grafana and Prometheus access
2. **Network Policies**: Restrict monitoring traffic
3. **RBAC**: Use appropriate service accounts
4. **Secrets Management**: Secure sensitive configuration
5. **TLS**: Use HTTPS for all monitoring endpoints

## Support

For monitoring issues:

1. Check the troubleshooting section above
2. Review Prometheus and Grafana logs
3. Verify configuration in values.yaml
4. Test metrics endpoints manually
5. Contact the DevOps team 