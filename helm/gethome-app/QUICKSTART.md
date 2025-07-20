# GetHome Application - Quick Start Guide

This guide helps you quickly deploy the GetHome application and understand its monitoring and observability setup.

---

## Prerequisites
- Kubernetes cluster (1.19+)
- Helm 3.0+
- kubectl configured to access your cluster
- NGINX Ingress Controller installed
- Cert-Manager installed (for TLS certificates)

---

## Quick Deployment

### 1. Clone the repository
```bash
git clone https://github.com/AET-DevOps25/team-gethome.git
cd team-gethome
```

### 2. Update image references
Edit the `values.yaml` file and update the image references to point to your Docker registry.

### 3. Deploy the application
```bash
kubectl create namespace devops25-k8s-gethome
helm install gethome-app ./helm/gethome-app --namespace devops25-k8s-gethome
```

### 4. Verify the deployment
```bash
kubectl get pods -n devops25-k8s-gethome
kubectl get services -n devops25-k8s-gethome
kubectl get ingress -n devops25-k8s-gethome
```

### 5. Access the application
- Frontend: `https://gethome.local` (or your configured domain)
- API: `https://gethome.local/api`

---

## Monitoring & Observability (Aligned with Dashboards)

### Monitoring Stack
- **Prometheus**: Metrics collection
- **Grafana**: Dashboards
- **AlertManager**: Alerting
- **Custom GetHome Exporter**: Business and advanced metrics

### Dashboards
- **Business Intelligence**: Incidents prevented, distance saved, safety score, engagement, adoption, optimization, etc.
- **System Performance**: Availability, API success, JVM/CPU/memory, request rates, resource forecasts, cost efficiency
- **Security & Safety**: Danger zone density, emergency response, false alarms, user safety profile, service up/down, alert volume
- **Operational Insights**: Peak usage, retention, cross-service latency, health overview, operational efficiency

### Key Metrics (examples)
- `gethome_estimated_incidents_prevented_total`
- `gethome_total_distance_saved_kilometers`
- `gethome_safety_score_average`
- `gethome_emergency_response_efficiency_score`
- `gethome_feature_adoption_rate_percentage`
- `gethome_user_engagement_score`
- `gethome_community_safety_contribution_score`
- `gethome_route_optimization_effectiveness_percentage`
- `gethome_false_alarm_rate_percentage`
- `gethome_emergency_network_strength_score`
- `gethome_route_planning_frequency_per_user_per_day`
- `gethome_peak_usage_prediction_factor`
- `gethome_system_availability_score`
- `gethome_api_success_rate_percentage`
- `gethome_resource_utilization_forecast_percentage`
- `gethome_cost_efficiency_score`
- `gethome_cross_service_latency_seconds`
- `up`

### Access
- **Grafana:** `http://localhost:3001` (admin/admin)
- **Prometheus:** `http://localhost:9090`

---

## Troubleshooting
- **Pods not starting:** `kubectl describe pod <pod> -n devops25-k8s-gethome`
- **Metrics missing:** Check Prometheus targets and exporter logs
- **Dashboards empty:** Ensure metrics are present in Prometheus and exporter/services are running
- **Alerts not firing:** Check AlertManager config and logs

---

## Best Practices
- Only expose metrics used in dashboards
- Keep metric names/labels consistent
- Regularly review dashboards and alerts
- Document alert runbooks and escalation paths

---

## Security Notes
- Change default passwords and secrets in production
- Use Kubernetes secrets for sensitive data
- Apply RBAC and network policies as needed

---

## Support
- Review this guide and dashboards
- For issues, check logs and Prometheus/Grafana UIs
- Contact the Platform team for help

---

**This guide is strictly aligned with the current monitoring stack and dashboards. For detailed monitoring info, see ADVANCED_MONITORING.md.** 