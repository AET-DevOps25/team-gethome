# GetHome Application - Quick Start Guide

This guide will help you quickly deploy the GetHome application to your Kubernetes cluster.

## Prerequisites

- Kubernetes cluster (1.19+)
- Helm 3.0+
- kubectl configured to access your cluster
- NGINX Ingress Controller installed
- Cert-Manager installed (for TLS certificates)

## Quick Deployment

### 1. Clone the repository
```bash
git clone <your-repo-url>
cd team-gethome
```

### 2. Update image references
Edit the `values.yaml` file and update the image references to point to your Docker registry:

```yaml
services:
  ai-service:
    image: your-registry/ai-service:latest
  auth-service:
    image: your-registry/auth-service:latest
  # ... update other services
```

### 3. Deploy the application

#### Option A: Using the deployment script (Recommended)
```bash
# Deploy to production
./helm/gethome-app/deploy.sh

# Deploy to development
./helm/gethome-app/deploy.sh -e development

# Deploy with custom namespace
./helm/gethome-app/deploy.sh -n my-namespace
```

#### Option B: Using Helm directly
```bash
# Create namespace
kubectl create namespace devops25-k8s-gethome

# Deploy the application
helm install gethome-app ./helm/gethome-app --namespace devops25-k8s-gethome

# Or deploy with development values
helm install gethome-app ./helm/gethome-app -f ./helm/gethome-app/values-dev.yaml --namespace devops25-k8s-gethome
```

### 4. Verify the deployment
```bash
# Check pods
kubectl get pods -n devops25-k8s-gethome

# Check services
kubectl get services -n devops25-k8s-gethome

# Check ingress
kubectl get ingress -n devops25-k8s-gethome
```

### 5. Access the application
Once deployed, you can access the application at:
- Frontend: `https://gethome.local` (or your configured domain)
- API: `https://gethome.local/api`

## Configuration

### Environment Variables
The application uses the following key environment variables:

- `MONGODB_URI`: MongoDB connection string
- `JWT_SECRET`: JWT signing secret (change in production!)
- `SPRING_PROFILES_ACTIVE`: Spring profile (development/production)

### Custom Values
Create a custom values file to override default settings:

```yaml
# custom-values.yaml
global:
  namespace: my-custom-namespace

services:
  auth-service:
    image: my-registry/auth-service:v1.0.0
    resources:
      limits:
        cpu: 1000m
        memory: 2Gi

ingress:
  hosts:
    - host: my-domain.com
      paths:
        - path: /
          pathType: Prefix
          service: react-client
```

Deploy with custom values:
```bash
helm install gethome-app ./helm/gethome-app -f custom-values.yaml --namespace devops25-k8s-gethome
```

## Troubleshooting

### Common Issues

1. **Pods not starting**
   ```bash
   kubectl describe pod <pod-name> -n devops25-k8s-gethome
   kubectl logs <pod-name> -n devops25-k8s-gethome
   ```

2. **Services not accessible**
   ```bash
   kubectl get endpoints -n devops25-k8s-gethome
   kubectl describe service <service-name> -n devops25-k8s-gethome
   ```

3. **Ingress not working**
   ```bash
   kubectl describe ingress gethome-ingress -n devops25-k8s-gethome
   kubectl get events -n devops25-k8s-gethome
   ```

### Health Checks
All services include health check endpoints:
- Auth Service: `/actuator/health`
- User Management Service: `/actuator/health`
- AI Service: `/health`

### Resource Monitoring
```bash
# Check resource usage
kubectl top pods -n devops25-k8s-gethome

# Check node resources
kubectl top nodes
```

## Scaling

### Horizontal Pod Autoscaler
Enable automatic scaling by setting `hpa.enabled: true` in your values file:

```yaml
hpa:
  enabled: true
  minReplicas: 1
  maxReplicas: 10
  targetCPUUtilizationPercentage: 80
```

### Manual Scaling
```bash
# Scale auth service to 3 replicas
kubectl scale deployment auth-service --replicas=3 -n devops25-k8s-gethome
```

## Upgrading

### Upgrade the application
```bash
# Update the chart
helm upgrade gethome-app ./helm/gethome-app --namespace devops25-k8s-gethome

# Or with custom values
helm upgrade gethome-app ./helm/gethome-app -f custom-values.yaml --namespace devops25-k8s-gethome
```

### Rollback
```bash
# List releases
helm list -n devops25-k8s-gethome

# Rollback to previous version
helm rollback gethome-app 1 -n devops25-k8s-gethome
```

## Cleanup

### Uninstall the application
```bash
# Uninstall Helm release
helm uninstall gethome-app -n devops25-k8s-gethome

# Delete namespace (optional)
kubectl delete namespace devops25-k8s-gethome

# Delete persistent volumes (optional - will delete all data)
kubectl delete pvc mongo-pvc -n devops25-k8s-gethome
```

## Security Notes

1. **Change default passwords**: Update MongoDB password and JWT secret in production
2. **Use secrets**: Store sensitive data in Kubernetes secrets
3. **Network policies**: Consider implementing network policies for service-to-service communication
4. **RBAC**: Configure appropriate RBAC for service accounts

## Support

For issues and questions:
1. Check the troubleshooting section above
2. Review the logs: `kubectl logs -f deployment/<service-name> -n devops25-k8s-gethome`
3. Check the full documentation in `README.md`
4. Open an issue in the project repository 