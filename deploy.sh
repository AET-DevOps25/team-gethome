#!/bin/bash

# GetHome Application Deployment Script
# This script builds and deploys all services together

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
NAMESPACE="devops25-k8s-gethome"
REGISTRY="ghcr.io/aet-devops25/team-gethome"
VERSION=${1:-latest}
ENVIRONMENT=${2:-production}

echo -e "${BLUE}🚀 GetHome Application Deployment${NC}"
echo -e "${BLUE}Version: ${VERSION}${NC}"
echo -e "${BLUE}Environment: ${ENVIRONMENT}${NC}"
echo -e "${BLUE}Registry: ${REGISTRY}${NC}"
echo ""

# Function to print status
print_status() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check prerequisites
echo -e "${BLUE}🔍 Checking prerequisites...${NC}"

if ! command_exists docker; then
    print_error "Docker is not installed"
    exit 1
fi

if ! command_exists kubectl; then
    print_error "kubectl is not installed"
    exit 1
fi

if ! command_exists helm; then
    print_error "Helm is not installed"
    exit 1
fi

print_status "All prerequisites are installed"

# Check if logged into Docker registry
echo -e "${BLUE}🔐 Checking Docker registry access...${NC}"
if ! docker info >/dev/null 2>&1; then
    print_error "Docker is not running"
    exit 1
fi

print_status "Docker is running"

# Build and push all services
echo -e "${BLUE}🏗️  Building and pushing services...${NC}"

# Build React Client
echo -e "${BLUE}📱 Building React Client...${NC}"
cd client
docker build -t ${REGISTRY}/react-client:${VERSION} .
docker push ${REGISTRY}/react-client:${VERSION}
print_status "React Client built and pushed"
cd ..

# Build Auth Service
echo -e "${BLUE}🔐 Building Auth Service...${NC}"
cd server/auth-service
chmod +x ./gradlew
./gradlew build -x test
docker build -t ${REGISTRY}/auth-service:${VERSION} .
docker push ${REGISTRY}/auth-service:${VERSION}
print_status "Auth Service built and pushed"
cd ../..

# Build User Management Service
echo -e "${BLUE}👥 Building User Management Service...${NC}"
cd server/usermanagement-service
chmod +x ./gradlew
./gradlew build -x test
docker build -t ${REGISTRY}/usermanagement-service:${VERSION} .
docker push ${REGISTRY}/usermanagement-service:${VERSION}
print_status "User Management Service built and pushed"
cd ../..

# Build Message Service
echo -e "${BLUE}📧 Building Message Service...${NC}"
cd server/message-service
chmod +x ./gradlew
./gradlew build -x test
docker build -t ${REGISTRY}/message-service:${VERSION} .
docker push ${REGISTRY}/message-service:${VERSION}
print_status "Message Service built and pushed"
cd ../..

# Build Routing Service
echo -e "${BLUE}🗺️  Building Routing Service...${NC}"
cd server/routing-service
chmod +x ./gradlew
./gradlew build -x test
docker build -t ${REGISTRY}/routing-service:${VERSION} .
docker push ${REGISTRY}/routing-service:${VERSION}
print_status "Routing Service built and pushed"
cd ../..

# Build AI Service
echo -e "${BLUE}🤖 Building AI Service...${NC}"
cd server/ai-service
docker build -t ${REGISTRY}/ai-service:${VERSION} .
docker push ${REGISTRY}/ai-service:${VERSION}
print_status "AI Service built and pushed"
cd ../..

print_status "All services built and pushed successfully"

# Deploy to Kubernetes
echo -e "${BLUE}🚀 Deploying to Kubernetes...${NC}"

# Create namespace if it doesn't exist
kubectl create namespace ${NAMESPACE} --dry-run=client -o yaml | kubectl apply -f -

# Update Helm values with current version
echo -e "${BLUE}📝 Updating Helm values...${NC}"
cd helm/gethome-app

# Update image tags in values file
if [ "$ENVIRONMENT" = "production" ]; then
    VALUES_FILE="values.yaml"
else
    VALUES_FILE="values-dev.yaml"
fi

# Update image tags
sed -i.bak "s|image: ${REGISTRY}/.*:.*|image: ${REGISTRY}/\1:${VERSION}|g" ${VALUES_FILE}

# Deploy using Helm
echo -e "${BLUE}📦 Deploying with Helm...${NC}"
helm upgrade --install gethome-app . \
    --namespace ${NAMESPACE} \
    --values ${VALUES_FILE} \
    --set global.environment=${ENVIRONMENT} \
    --wait \
    --timeout=10m

print_status "Helm deployment completed"

# Wait for all pods to be ready
echo -e "${BLUE}⏳ Waiting for all pods to be ready...${NC}"
kubectl wait --for=condition=ready pod -l app.kubernetes.io/instance=gethome-app -n ${NAMESPACE} --timeout=300s

print_status "All pods are ready"

# Check service health
echo -e "${BLUE}🏥 Checking service health...${NC}"

# Get service URLs
AUTH_URL=$(kubectl get svc -n ${NAMESPACE} auth-service -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || echo "localhost")
USER_URL=$(kubectl get svc -n ${NAMESPACE} usermanagement-service -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || echo "localhost")
MESSAGE_URL=$(kubectl get svc -n ${NAMESPACE} message-service -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || echo "localhost")
ROUTING_URL=$(kubectl get svc -n ${NAMESPACE} routing-service -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || echo "localhost")
AI_URL=$(kubectl get svc -n ${NAMESPACE} ai-service -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || echo "localhost")

# Health check function
check_health() {
    local service=$1
    local port=$2
    local url=$3
    
    echo -n "Checking ${service}... "
    if curl -f -s "http://${url}:${port}/actuator/health" >/dev/null 2>&1; then
        print_status "${service} is healthy"
        return 0
    else
        print_warning "${service} health check failed"
        return 1
    fi
}

# Check each service
check_health "Auth Service" "8081" "${AUTH_URL}"
check_health "User Management Service" "8082" "${USER_URL}"
check_health "Message Service" "8083" "${MESSAGE_URL}"
check_health "Routing Service" "8084" "${ROUTING_URL}"

# Check AI service (different endpoint)
echo -n "Checking AI Service... "
if curl -f -s "http://${AI_URL}:8085/" >/dev/null 2>&1; then
    print_status "AI Service is healthy"
else
    print_warning "AI Service health check failed"
fi

# Display deployment information
echo ""
echo -e "${GREEN}🎉 Deployment completed successfully!${NC}"
echo ""
echo -e "${BLUE}📊 Deployment Summary:${NC}"
echo -e "  Namespace: ${NAMESPACE}"
echo -e "  Version: ${VERSION}"
echo -e "  Environment: ${ENVIRONMENT}"
echo ""
echo -e "${BLUE}🔗 Service URLs:${NC}"
echo -e "  Auth Service: http://${AUTH_URL}:8081"
echo -e "  User Management: http://${USER_URL}:8082"
echo -e "  Message Service: http://${MESSAGE_URL}:8083"
echo -e "  Routing Service: http://${ROUTING_URL}:8084"
echo -e "  AI Service: http://${AI_URL}:8085"
echo ""
echo -e "${BLUE}📋 Useful Commands:${NC}"
echo -e "  View pods: kubectl get pods -n ${NAMESPACE}"
echo -e "  View services: kubectl get svc -n ${NAMESPACE}"
echo -e "  View logs: kubectl logs -f deployment/[service-name] -n ${NAMESPACE}"
echo -e "  Port forward: kubectl port-forward svc/[service-name] [local-port]:[service-port] -n ${NAMESPACE}"
echo ""

# Cleanup
cd ../..
rm -f helm/gethome-app/${VALUES_FILE}.bak

print_status "Deployment script completed" 