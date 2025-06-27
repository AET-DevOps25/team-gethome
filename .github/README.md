# GitHub Actions Workflows

This directory contains GitHub Actions workflows for the GetHome application CI/CD pipeline.

## Workflows Overview

### 1. `deploy.yml` - Main Deployment Pipeline
**Triggers:**
- Push to `main` branch → Deploy to production
- Push to `develop` branch → Deploy to development
- Pull requests to `main` → Build and test only
- Manual trigger with environment selection

**Jobs:**
- **test-and-build**: Builds and tests all services, pushes Docker images
- **deploy-development**: Deploys to development environment
- **deploy-production**: Deploys to production environment
- **security-scan**: Runs security vulnerability scans
- **notify**: Sends deployment notifications

### 2. `pr-validation.yml` - Pull Request Validation
**Triggers:**
- Pull requests to `main` or `develop`
- Push to `main` or `develop`

**Jobs:**
- **lint**: Code linting and formatting checks
- **test**: Unit and integration tests
- **security**: Security vulnerability scanning
- **build**: Docker image building (without pushing)
- **k8s-validation**: Kubernetes manifest validation
- **performance**: Basic performance testing
- **comment**: Comments PR with validation results

### 3. `maintenance.yml` - Scheduled Maintenance
**Triggers:**
- Weekly schedule (Sundays at 2 AM UTC)
- Manual trigger with task selection

**Jobs:**
- **update-dependencies**: Updates project dependencies
- **security-audit**: Comprehensive security scanning
- **cleanup**: Cleans up old resources
- **health-check**: Verifies environment health
- **report**: Generates maintenance report

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

#### Container Registry
The workflows use GitHub Container Registry (ghcr.io) by default. If using a different registry:

```bash
# Docker registry credentials
DOCKER_USERNAME=<your-docker-username>
DOCKER_PASSWORD=<your-docker-password>
```

### 2. Environment Protection Rules

Set up environment protection rules in GitHub:

#### Development Environment
- **Required reviewers**: 1
- **Wait timer**: 0 minutes
- **Deployment branches**: `develop`

#### Production Environment
- **Required reviewers**: 2
- **Wait timer**: 5 minutes
- **Deployment branches**: `main`

### 3. Branch Protection Rules

Configure branch protection for `main` and `develop`:

- **Require status checks to pass before merging**
- **Require branches to be up to date before merging**
- **Require pull request reviews before merging**
- **Require conversation resolution before merging**
- **Include administrators**

### 4. Required Status Checks

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
2. Select **Deploy GetHome Application**
3. Click **Run workflow**
4. Choose environment and namespace
5. Click **Run workflow**

### Pull Request Process

1. Create a feature branch
2. Make changes and push
3. Create pull request to `develop`
4. Wait for validation checks to pass
5. Get code review approval
6. Merge to `develop`

## Customization

### Adding New Services

1. Update the matrix in `deploy.yml`:
   ```yaml
   strategy:
     matrix:
       service: [auth-service, usermanagement-service, emergency-service, routing-service, ai-service, react-client, new-service]
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