# GitHub Actions Workflows

This directory contains GitHub Actions workflows for the GetHome application CI/CD pipeline.

## Workflows Overview

### 1. `deploy_AET.yml` - Main Deployment Pipeline
**Triggers:**
- Push to `main` or `develop` branches (when changes to helm/, server/, client/, or workflow file)
- Pull requests to `main` or `develop` (build and test only)
- Manual trigger with environment selection

**Jobs:**
- **test-and-build**: Builds and tests all services, pushes Docker images to GitHub Container Registry
  - Java services: auth-service, usermanagement-service, message-service, routing-service
  - Python services: ai-service, gethome-metrics-exporter
  - React client: react-client
- **deploy-development**: Deploys to development environment (commented out)
- **deploy-production**: Deploys to production environment (commented out)

### 2. `build-containers.yml` - Container Build Pipeline
**Triggers:**
- Push to `main` or `develop` branches (when changes to server/ or client/)
- Pull requests to `main` or `develop`
- Manual trigger with service and tag selection

**Jobs:**
- **build-java-services**: Builds Java microservices (auth, usermanagement, message, routing)
- **build-python-services**: Builds Python services (ai-service, gethome-metrics-exporter)
- **build-react-client**: Builds React frontend application

### 3. `pr-validation.yml` - Pull Request Validation
**Triggers:**
- Pull requests to `main` or `develop`
- Push to `main` or `develop`

**Jobs:**
- **lint**: Code linting and formatting checks for all services
- **test**: Unit and integration tests with coverage reporting
- **security**: Security vulnerability scanning (Trivy + Snyk)
- **build**: Docker image building (without pushing)
- **k8s-validation**: Kubernetes manifest validation

### 4. `ansible-docker-deploy.yml` - EC2 Deployment
**Triggers:**
- Manual trigger only

**Jobs:**
- **deploy**: Deploys to EC2 using Ansible playbook with Docker Compose

## Setup Instructions

### 1. Repository Secrets

Configure the following secrets in your GitHub repository:

#### Kubernetes Configuration
```bash
# Development cluster kubeconfig (base64 encoded)
KUBE_CONFIG_DEV=<base64-encoded-kubeconfig>

# Production cluster kubeconfig (base64 encoded)
KUBE_CONFIG_PROD=<base64-encoded-kubeconfig>
```

To encode your kubeconfig:
```bash
cat ~/.kube/config | base64 -w 0
```

#### Security Tokens
```bash
# Snyk security scanning token
SNYK_TOKEN=<your-snyk-token>

# Slack webhook URL (optional)
SLACK_WEBHOOK_URL=<your-slack-webhook-url>
```

#### EC2 Deployment
```bash
# EC2 SSH private key
EC2_SSH_PRIVATE_KEY=<your-ec2-private-key>

# EC2 public IP
PUBLIC_IP=<your-ec2-public-ip>

# Application secrets
JWT_SECRET=<your-jwt-secret>
MONGO_ROOT_PASSWORD=<your-mongo-password>
EMAIL_USERNAME=<your-email-username>
EMAIL_PASSWORD=<your-email-password>
TWILIO_ACCOUNT_SID=<your-twilio-sid>
TWILIO_AUTH_TOKEN=<your-twilio-token>
TWILIO_PHONE_NUMBER=<your-twilio-phone>
OPENAI_API_KEY=<your-openai-key>
OPENROUTE_API_KEY=<your-openroute-key>
```

### 2. Container Registry

The workflows use GitHub Container Registry (ghcr.io) by default:
- Registry: `ghcr.io`
- Image prefix: `aet-devops25/team-gethome`
- Authentication: Uses `GITHUB_TOKEN` automatically

### 3. Environment Protection Rules

Set up environment protection rules in GitHub:

#### Development Environment
- **Required reviewers**: 1
- **Wait timer**: 0 minutes
- **Deployment branches**: `develop`

#### Production Environment
- **Required reviewers**: 2
- **Wait timer**: 5 minutes
- **Deployment branches**: `main`

### 4. Branch Protection Rules

Configure branch protection for `main` and `develop`:

- **Require status checks to pass before merging**
- **Require branches to be up to date before merging**
- **Require pull request reviews before merging**
- **Require conversation resolution before merging**
- **Include administrators**

### 5. Required Status Checks

Add these status checks to branch protection:
- `test-and-build`
- `lint`
- `security`
- `k8s-validation`

## Usage

### Automatic Deployments

1. **Development**: Push to `develop` branch
   ```bash
   git checkout develop
   git push origin develop
   ```

2. **Production**: Push to `main` branch
   ```bash
   git checkout main
   git push origin main
   ```

### Manual Deployments

1. Go to **Actions** tab in GitHub
2. Select the desired workflow
3. Click **Run workflow**
4. Choose parameters and click **Run workflow**

### Pull Request Process

1. Create a feature branch
2. Make changes and push
3. Create pull request to `develop`
4. Wait for validation checks to pass
5. Get code review approval
6. Merge to `develop`

## Services Overview

### Java Microservices
- **auth-service**: Authentication and authorization
- **usermanagement-service**: User profile and management
- **message-service**: Messaging and notifications
- **routing-service**: Route planning and optimization

### Python Services
- **ai-service**: AI and machine learning features
- **gethome-metrics-exporter**: Custom Prometheus exporter for business metrics

### Frontend
- **react-client**: React-based web application

## Customization

### Adding New Services

1. Update the matrix in workflows:
   ```yaml
   strategy:
     matrix:
       service: [auth-service, usermanagement-service, message-service, routing-service, ai-service, gethome-metrics-exporter, react-client, new-service]
   ```

2. Add service-specific build steps if needed

### Modifying Deployment Environments

1. Update environment conditions in workflows
2. Add new environment secrets
3. Update Helm values files

### Adding Custom Tests

1. Add test steps in the appropriate workflow
2. Update branch protection rules
3. Configure test reporting

## Troubleshooting

### Common Issues

#### Build Failures
- Check Dockerfile syntax
- Verify dependencies are available
- Check resource limits

#### Deployment Failures
- Verify Kubernetes cluster access
- Check namespace permissions
- Review Helm chart syntax

#### Security Scan Failures
- Update vulnerable dependencies
- Review security policies
- Check for false positives

### Debug Commands

```bash
# Check workflow logs
gh run list
gh run view <run-id>

# Check deployment status
kubectl get pods -n devops25-k8s-gethome
kubectl logs -f deployment/<service-name> -n devops25-k8s-gethome

# Check Helm releases
helm list -n devops25-k8s-gethome
helm status gethome-app -n devops25-k8s-gethome
```

### Monitoring

#### GitHub Actions
- Monitor workflow success rates
- Review build times
- Check resource usage

#### Kubernetes
- Monitor pod health
- Check resource utilization
- Review logs for errors

#### Security
- Review security scan results
- Monitor vulnerability reports
- Track dependency updates

## Best Practices

### Security
- Use least privilege for service accounts
- Regularly update dependencies
- Scan for vulnerabilities
- Use secrets for sensitive data

### Performance
- Optimize Docker images
- Use multi-stage builds
- Cache dependencies
- Parallelize jobs where possible

### Reliability
- Use health checks
- Implement rollback strategies
- Monitor deployments
- Test in staging first

### Maintenance
- Regular dependency updates
- Clean up old resources
- Monitor resource usage
- Keep documentation updated

## Support

For issues with the CI/CD pipeline:

1. Check workflow logs in GitHub Actions
2. Review this documentation
3. Check Kubernetes cluster status
4. Contact the DevOps team

## Contributing

To contribute to the CI/CD pipeline:

1. Create a feature branch
2. Make changes to workflow files
3. Test locally if possible
4. Create a pull request
5. Get review and approval
6. Merge changes 