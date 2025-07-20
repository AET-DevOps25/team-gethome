# GetHome Advanced Monitoring System (Dashboard-Aligned)

## Overview

The GetHome monitoring stack provides real-time visibility into business, safety, operational, and system health using:
- **Prometheus** for metrics collection
- **Grafana** for dashboards
- **AlertManager** for alerting
- **Custom GetHome Exporter** for business and advanced metrics

---

## Dashboards & Their Focus

### 1. Business Intelligence Dashboard
- **Panels:**
  - Incidents Prevented (`gethome_estimated_incidents_prevented_total`)
  - Distance Saved (`gethome_total_distance_saved_kilometers`)
  - Safety Score (`gethome_safety_score_average`)
  - Emergency Response Efficiency (`gethome_emergency_response_efficiency_score`)
  - Feature Adoption Rates (`gethome_feature_adoption_rate_percentage`)
  - User Engagement Score (`gethome_user_engagement_score`)
  - Safety Conscious Users (`gethome_safety_conscious_users_percentage`)
  - Community Safety Contribution (`gethome_community_safety_contribution_score`)
  - Route Optimization Effectiveness (`gethome_route_optimization_effectiveness_percentage`)
  - False Alarm Rate (`gethome_false_alarm_rate_percentage`)
  - Emergency Network Strength (`gethome_emergency_network_strength_score`)
  - Route Planning Frequency (`gethome_route_planning_frequency_per_user_per_day`)
  - Peak Usage Prediction (`gethome_peak_usage_prediction_factor`)

### 2. System Performance Dashboard
- **Panels:**
  - System Availability (`gethome_system_availability_score`)
  - API Success Rate (`gethome_api_success_rate_percentage`)
  - JVM Memory Usage (`jvm_memory_used_bytes`)
  - CPU Usage (`process_cpu_seconds_total`)
  - HTTP Request Rate (`rate(http_requests_total[5m])`)
  - HTTP Request Duration (`histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m]))`)
  - Data Freshness (`gethome_data_freshness_minutes`)
  - JVM Threads & GC (`jvm_threads_current`, `rate(jvm_gc_collection_seconds_count[5m])`)
  - Resource Utilization Forecast (`gethome_resource_utilization_forecast_percentage`)
  - Cost Efficiency Score (`gethome_cost_efficiency_score`)

### 3. Security & Safety Monitoring Dashboard
- **Panels:**
  - Danger Zone Density (`gethome_danger_zones_density_per_km2`)
  - Emergency Response Efficiency (`gethome_emergency_response_efficiency_score`)
  - False Alarm Rate (`gethome_false_alarm_rate_percentage`)
  - User Safety Profile (`gethome_user_safety_profile_score`)
  - Emergency Network Strength (`gethome_emergency_network_strength_score`)
  - Safety Improvement Trend (`gethome_safety_improvement_trend_percentage`)
  - Authentication Security Events (`rate(http_requests_total{uri=~"/auth/.*"}[5m])`)
  - Critical Service Availability (`up{job=~".*routing.*|.*message.*|.*auth.*"}`)
  - Emergency Alert Volume (`rate(http_requests_total{uri=~"/emergency/.*"}[5m])`)

### 4. Operational Insights Dashboard
- **Panels:**
  - Peak Usage Prediction (`gethome_peak_usage_prediction_factor`)
  - Cost Efficiency Score (`gethome_cost_efficiency_score`)
  - User Retention Rate (`gethome_user_retention_rate_percentage`)
  - Resource Utilization Forecast (`gethome_resource_utilization_forecast_percentage`)
  - Business Impact Metrics (`gethome_estimated_incidents_prevented_total`, `gethome_total_distance_saved_kilometers`)
  - Cross-Service Latency (`gethome_cross_service_latency_seconds`)
  - Service Health Overview (`up`)
  - User Engagement Analytics (`gethome_user_engagement_score`, `gethome_community_safety_contribution_score`)
  - Operational Efficiency (`gethome_route_optimization_effectiveness_percentage`, `gethome_system_availability_score`)

---

## Key Metrics (by Dashboard)

**Business/Engagement:**
- `gethome_estimated_incidents_prevented_total`
- `gethome_total_distance_saved_kilometers`
- `gethome_safety_score_average`
- `gethome_emergency_response_efficiency_score`
- `gethome_feature_adoption_rate_percentage`
- `gethome_user_engagement_score`
- `gethome_safety_conscious_users_percentage`
- `gethome_community_safety_contribution_score`
- `gethome_route_optimization_effectiveness_percentage`
- `gethome_false_alarm_rate_percentage`
- `gethome_emergency_network_strength_score`
- `gethome_route_planning_frequency_per_user_per_day`
- `gethome_peak_usage_prediction_factor`
- `gethome_user_retention_rate_percentage`
- `gethome_user_safety_profile_score`
- `gethome_safety_improvement_trend_percentage`

**System/Operational:**
- `gethome_system_availability_score`
- `gethome_api_success_rate_percentage`
- `jvm_memory_used_bytes`
- `process_cpu_seconds_total`
- `rate(http_requests_total[5m])`
- `histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m]))`
- `gethome_data_freshness_minutes`
- `jvm_threads_current`
- `rate(jvm_gc_collection_seconds_count[5m])`
- `gethome_resource_utilization_forecast_percentage`
- `gethome_cost_efficiency_score`
- `gethome_cross_service_latency_seconds`
- `up`

**Security/Safety:**
- `gethome_danger_zones_density_per_km2`
- `rate(http_requests_total{uri=~"/auth/.*"}[5m])`
- `up{job=~".*routing.*|.*message.*|.*auth.*"}`
- `rate(http_requests_total{uri=~"/emergency/.*"}[5m])`

---

## Alerting (Example Policies)
- **Critical:** Safety score <0.6, response efficiency <0.7, service down, high error rate
- **Warning:** Engagement drop, low feature adoption, optimization ineffective
- **Info:** Peak usage predicted, cost efficiency declining

---

## Setup & Access
- **Enable monitoring in values.yaml** and deploy with Helm.
- **Grafana:** `http://localhost:3001` (admin/admin123)
- **Prometheus:** `http://localhost:9090`

---

## Troubleshooting
- **Missing Data:** Check if the metric is present in Prometheus (`/api/v1/label/__name__/values`).
- **Panel Empty:** Ensure the exporter/service is running and exposing the metric.
- **Alerts Not Firing:** Check AlertManager config and logs.
- **Grafana Issues:** Check data source and dashboard provisioning.

---

## Best Practices
- Only expose metrics that are used in dashboards.
- Keep metric names and labels consistent.
- Regularly review dashboards for relevance.
- Document alert runbooks and escalation paths.

---

## Onboarding
- Review this document and the dashboards.
- Join alert channels.
- Practice using Prometheus and Grafana for metric queries and troubleshooting.

---

**This document is strictly aligned with your current dashboards and the metrics/panels they use. If you add or change dashboards, update this doc accordingly!** 