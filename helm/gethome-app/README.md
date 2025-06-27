# GetHome Application Helm Chart

This Helm chart deploys the complete GetHome microservices application to Kubernetes.

## Overview

The GetHome application consists of the following components:

- **MongoDB**: Database for storing application data
- **Auth Service**: Authentication and authorization service
- **User Management Service**: User profile and management service
- **AI Service**: Artificial intelligence and machine learning service
- **Emergency Service**: Emergency response and notification service
- **Routing Service**: Route planning and navigation service
- **React Client**: Frontend web application

## Prerequisites

- Kubernetes 1.19+
- Helm 3.0+
- NGINX Ingress Controller
- Cert-Manager (for TLS certificates)

## Installation

### 1. Add the Helm repository (if applicable)
```bash
helm repo add gethome https://your-helm-repo.com
helm repo update
```

### 2. Install the chart
```bash
# Install with default values
helm install gethome-app ./helm/gethome-app

# Install with custom values
helm install gethome-app ./helm/gethome-app -f values-custom.yaml

# Install in a specific namespace
helm install gethome-app ./helm/gethome-app --namespace devops25-k8s-gethome --create-namespace
```

### 3. Verify the installation
```bash
kubectl get pods -n devops25-k8s-gethome
kubectl get services -n devops25-k8s-gethome
kubectl get ingress -n devops25-k8s-gethome
```

## Configuration

### Global Configuration

| Parameter | Description | Default |
|-----------|-------------|---------|
| `global.namespace` | Kubernetes namespace | `devops25-k8s-gethome` |
| `global.environment` | Environment name | `production` |
| `global.imageRegistry` | Docker registry | `""` |
| `global.imagePullSecrets` | Image pull secrets | `[]` |

### MongoDB Configuration

| Parameter | Description | Default |
|-----------|-------------|---------|
| `mongo.enabled` | Enable MongoDB | `true` |
| `mongo.image` | MongoDB image | `mongo:6` |
| `mongo.username` | MongoDB username | `root` |
| `mongo.password` | MongoDB password | `example` |
| `mongo.database` | Database name | `gethome` |
| `mongo.storage.size` | Storage size | `10Gi` |

### Service Configuration

Each service can be configured with the following parameters:

| Parameter | Description | Default |
|-----------|-------------|---------|
| `services.<service>.enabled` | Enable service | `true` |
| `services.<service>.image` | Service image | `your-dockerhub/<service>:latest` |
| `services.<service>.port` | Service port | Varies by service |
| `services.<service>.resources` | Resource limits and requests | See values.yaml |

### Ingress Configuration

| Parameter | Description | Default |
|-----------|-------------|---------|
| `ingress.enabled` | Enable ingress | `true` |
| `ingress.className` | Ingress class | `nginx` |
| `ingress.hosts` | Ingress hosts | `gethome.local` |

## Environment Variables

### Auth Service
- `SPRING_PROFILES_ACTIVE`: Spring profile
- `SPRING_DATA_MONGODB_URI`: MongoDB connection string
- `JWT_SECRET`: JWT signing secret
- `JWT_EXPIRATION`: JWT expiration time

### User Management Service
- `SPRING_PROFILES_ACTIVE`: Spring profile
- `SPRING_DATA_MONGODB_URI`: MongoDB connection string
- `AUTH_SERVICE_URL`: Auth service URL

### AI Service
- `MONGODB_URI`: MongoDB connection string
- `AI_MODEL_PATH`: Path to AI models

### React Client
- `REACT_APP_API_BASE_URL`: API base URL
- `REACT_APP_AUTH_SERVICE_URL`: Auth service URL

## Security

The chart includes several security features:

- **Pod Security Context**: Non-root user execution
- **Container Security Context**: Read-only root filesystem, dropped capabilities
- **Service Account**: Dedicated service account for the application
- **Secrets Management**: Kubernetes secrets for sensitive data

## Scaling

### Horizontal Pod Autoscaler

Enable HPA by setting `hpa.enabled: true`:

```yaml
hpa:
  enabled: true
  minReplicas: 1
  maxReplicas: 10
  targetCPUUtilizationPercentage: 80
  targetMemoryUtilizationPercentage: 80
```

### Manual Scaling

Scale individual services:

```bash
kubectl scale deployment auth-service --replicas=3 -n devops25-k8s-gethome
```

## Monitoring

### Health Checks

All services include liveness and readiness probes:

- **Liveness Probe**: Checks if the service is alive
- **Readiness Probe**: Checks if the service is ready to receive traffic

### Resource Monitoring

Monitor resource usage:

```bash
kubectl top pods -n devops25-k8s-gethome
kubectl top nodes
```

## Troubleshooting

### Common Issues

1. **Pods not starting**: Check resource limits and requests
2. **Services not accessible**: Verify ingress configuration
3. **Database connection issues**: Check MongoDB service and credentials

### Debug Commands

```bash
# Check pod logs
kubectl logs -f deployment/auth-service -n devops25-k8s-gethome

# Check service endpoints
kubectl get endpoints -n devops25-k8s-gethome

# Check ingress status
kubectl describe ingress gethome-ingress -n devops25-k8s-gethome
```

## Upgrading

### Upgrade the chart
```bash
helm upgrade gethome-app ./helm/gethome-app
```

### Rollback
```bash
helm rollback gethome-app 1
```

## Uninstalling

```bash
helm uninstall gethome-app -n devops25-k8s-gethome
```

**Note**: This will not delete persistent volumes. To completely remove all data:

```bash
kubectl delete pvc mongo-pvc -n devops25-k8s-gethome
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test the chart
5. Submit a pull request

## License

This project is licensed under the MIT License. 