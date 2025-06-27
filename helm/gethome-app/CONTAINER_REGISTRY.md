# GitHub Container Registry (GHCR) Setup Guide

This guide explains how to use GitHub Container Registry (GHCR) for building, storing, and deploying your GetHome application containers.

## Overview

GitHub Container Registry provides a secure, reliable way to store and manage your Docker containers directly in your GitHub repository. This setup automatically builds containers on code changes and makes them available for deployment.

## Architecture

```
GitHub Repository
├── Code Changes
├── GitHub Actions (build-containers.yml)
├── GitHub Container Registry (ghcr.io)
└── Kubernetes Deployment (Helm Charts)
```

## Prerequisites

1. **GitHub Repository**: Your code must be in a GitHub repository
2. **GitHub Actions**: Enabled in your repository
3. **GitHub Token**: Automatically available as `GITHUB_TOKEN` secret
4. **Kubernetes Cluster**: Access to deploy containers

## Container Images

The following images are automatically built and pushed to GHCR:

### Java Services
- `ghcr.io/AET-DevOps25/team-gethome/auth-service`
- `ghcr.io/AET-DevOps25/team-gethome/usermanagement-service`
- `ghcr.io/AET-DevOps25/team-gethome/emergency-service`
- `ghcr.io/AET-DevOps25/team-gethome/routing-service`

### Python Service
- `ghcr.io/AET-DevOps25/team-gethome/ai-service`

### Frontend
- `ghcr.io/AET-DevOps25/team-gethome/react-client`

## Image Tags

Images are tagged with the following strategy:

- `latest`: Latest stable version (main branch)
- `main`: Main branch builds
- `develop`: Development branch builds
- `<commit-sha>`: Specific commit builds (e.g., `main-abc1234`)
- `v1.0.0`: Semantic version tags (manual)

## Setup Instructions

### 1. Repository Configuration

Ensure your repository has the correct structure:

```
team-gethome/
├── server/
│   ├── auth-service/
│   ├── usermanagement-service/
│   ├── emergency-service/
│   ├── routing-service/
│   └── ai-service/
├── client/
└── .github/workflows/build-containers.yml
```

### 2. GitHub Actions Workflow

The `build-containers.yml` workflow automatically:

- Triggers on code changes to server/ or client/ directories
- Builds multi-platform images (linux/amd64, linux/arm64)
- Pushes to GitHub Container Registry
- Uses GitHub Actions cache for faster builds
- Generates comprehensive metadata

### 3. Manual Container Building

You can manually trigger container builds:

1. Go to your GitHub repository
2. Navigate to Actions → Build and Push Containers
3. Click "Run workflow"
4. Optionally specify:
   - Service name (leave empty for all)
   - Custom tag

### 4. Container Registry Access

#### Public Access (Recommended)
```bash
# Pull images without authentication
docker pull ghcr.io/AET-DevOps25/team-gethome/auth-service:latest
```

#### Private Access (if needed)
```bash
# Login to GHCR
echo $GITHUB_TOKEN | docker login ghcr.io -u USERNAME --password-stdin

# Pull private images
docker pull ghcr.io/AET-DevOps25/team-gethome/auth-service:latest
```

## Helm Chart Integration

### 1. Update Values Files

The Helm charts are configured to use GHCR images by default:

```yaml
# values.yaml (Production)
services:
  auth-service:
    image: ghcr.io/AET-DevOps25/team-gethome/auth-service:latest

# values-dev.yaml (Development)
services:
  auth-service:
    image: ghcr.io/AET-DevOps25/team-gethome/auth-service:develop
```

### 2. Deploy with Helm

```bash
# Development deployment
helm upgrade --install gethome-app ./helm/gethome-app \
  --namespace devops25-k8s-gethome \
  -f ./helm/gethome-app/values-dev.yaml

# Production deployment
helm upgrade --install gethome-app ./helm/gethome-app \
  --namespace devops25-k8s-gethome \
  -f ./helm/gethome-app/values.yaml
```

### 3. Update Image Tags

To deploy specific versions:

```bash
# Deploy specific commit
helm upgrade gethome-app ./helm/gethome-app \
  --set services.auth-service.image=ghcr.io/AET-DevOps25/team-gethome/auth-service:main-abc1234

# Deploy specific version
helm upgrade gethome-app ./helm/gethome-app \
  --set services.auth-service.image=ghcr.io/AET-DevOps25/team-gethome/auth-service:v1.0.0
```

## CI/CD Pipeline Integration

### 1. Automated Deployment

The deployment workflow automatically uses the latest images:

```yaml
# .github/workflows/deploy.yml
- name: Deploy to Development
  run: |
    helm upgrade --install gethome-app ./helm/gethome-app \
      --namespace devops25-k8s-gethome \
      -f ./helm/gethome-app/values-dev.yaml
```

### 2. Image Tag Strategy

- **Development**: Uses `develop` tag for latest development builds
- **Staging**: Uses `main` tag for main branch builds
- **Production**: Uses `latest` tag for stable releases

## Monitoring and Management

### 1. View Container Images

Visit your GitHub repository → Packages to see all container images.

### 2. Image Details

Each image shows:
- Tags and versions
- Build history
- Size and layers
- Security vulnerabilities (if any)

### 3. Cleanup Old Images

```bash
# List images
gh api repos/AET-DevOps25/team-gethome/packages

# Delete old images (use with caution)
gh api repos/AET-DevOps25/team-gethome/packages/container/IMAGE_NAME/versions/VERSION_ID \
  --method DELETE
```

## Security Best Practices

### 1. Image Scanning

Enable GitHub's built-in security scanning:

1. Go to repository Settings → Security & analysis
2. Enable "Dependency graph" and "Dependabot alerts"
3. Enable "Code scanning" for container images

### 2. Access Control

- Use `GITHUB_TOKEN` for automated workflows
- Limit access to container registry
- Regularly rotate tokens

### 3. Image Signing

Consider signing images for production:

```bash
# Sign images with cosign
cosign sign ghcr.io/AET-DevOps25/team-gethome/auth-service:latest
```

## Troubleshooting

### Common Issues

#### 1. Build Failures
```bash
# Check workflow logs
gh run list --workflow=build-containers.yml

# View specific run
gh run view RUN_ID --log
```

#### 2. Image Pull Errors
```bash
# Check image exists
docker pull ghcr.io/AET-DevOps25/team-gethome/auth-service:latest

# Verify permissions
gh auth status
```

#### 3. Helm Deployment Issues
```bash
# Check image availability
kubectl describe pod -n devops25-k8s-gethome

# Verify image pull policy
kubectl get deployment auth-service -o yaml
```

### Debug Commands

```bash
# List all images in registry
gh api repos/AET-DevOps25/team-gethome/packages

# Check specific image tags
gh api repos/AET-DevOps25/team-gethome/packages/container/auth-service/versions

# Verify image metadata
docker inspect ghcr.io/AET-DevOps25/team-gethome/auth-service:latest
```

## Cost Optimization

### 1. Image Size Optimization

- Use multi-stage builds
- Remove unnecessary dependencies
- Use .dockerignore files
- Optimize base images

### 2. Storage Management

- Regularly clean up old images
- Use specific tags instead of `latest`
- Monitor storage usage

### 3. Build Optimization

- Use GitHub Actions cache
- Parallel builds where possible
- Optimize Dockerfile layers

## Migration from Other Registries

### From Docker Hub

1. Update image references in values files
2. Update CI/CD pipelines
3. Migrate existing images (if needed)
4. Update documentation

### From Private Registries

1. Configure authentication
2. Update image pull secrets
3. Test image access
4. Update deployment scripts

## Advanced Configuration

### 1. Custom Build Contexts

Modify the workflow for custom build contexts:

```yaml
- name: Build and push Docker image
  uses: docker/build-push-action@v5
  with:
    context: ./custom-context
    file: ./custom-context/Dockerfile.custom
```

### 2. Multi-Architecture Builds

The workflow already supports multi-arch builds:

```yaml
platforms: linux/amd64,linux/arm64
```

### 3. Build Arguments

Add build arguments for different environments:

```yaml
build-args: |
  BUILD_ENV=production
  VERSION=${{ github.sha }}
```

## Support and Resources

- [GitHub Container Registry Documentation](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Docker Buildx Documentation](https://docs.docker.com/buildx/)
- [Helm Documentation](https://helm.sh/docs/)

## Next Steps

1. **Set up monitoring**: Configure Prometheus and Grafana
2. **Implement security scanning**: Enable vulnerability scanning
3. **Add image signing**: Implement cosign for production
4. **Optimize builds**: Reduce image sizes and build times
5. **Set up rollbacks**: Implement automated rollback strategies 