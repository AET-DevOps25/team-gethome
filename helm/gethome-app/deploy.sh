#!/bin/bash

# GetHome Application Deployment Script
# This script deploys the GetHome application to Kubernetes using Helm

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default values
NAMESPACE="devops25-k8s-gethome"
CHART_PATH="./helm/gethome-app"
RELEASE_NAME="gethome-app"
ENVIRONMENT="production"
VALUES_FILE=""

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to show usage
show_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  -n, --namespace NAME     Kubernetes namespace (default: devops25-k8s-gethome)"
    echo "  -r, --release NAME       Helm release name (default: gethome-app)"
    echo "  -e, --environment ENV    Environment: production, development (default: production)"
    echo "  -f, --values FILE        Custom values file"
    echo "  -h, --help               Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0                                    # Deploy with default settings"
    echo "  $0 -e development                     # Deploy development environment"
    echo "  $0 -n my-namespace -r my-release      # Deploy with custom namespace and release name"
    echo "  $0 -f custom-values.yaml              # Deploy with custom values file"
}

# Function to check prerequisites
check_prerequisites() {
    print_status "Checking prerequisites..."
    
    # Check if kubectl is installed
    if ! command -v kubectl &> /dev/null; then
        print_error "kubectl is not installed. Please install kubectl first."
        exit 1
    fi
    
    # Check if helm is installed
    if ! command -v helm &> /dev/null; then
        print_error "Helm is not installed. Please install Helm first."
        exit 1
    fi
    
    # Check if kubectl can connect to cluster
    if ! kubectl cluster-info &> /dev/null; then
        print_error "Cannot connect to Kubernetes cluster. Please check your kubeconfig."
        exit 1
    fi
    
    print_success "Prerequisites check passed"
}

# Function to create namespace if it doesn't exist
create_namespace() {
    print_status "Checking if namespace '$NAMESPACE' exists..."
    
    if ! kubectl get namespace "$NAMESPACE" &> /dev/null; then
        print_status "Creating namespace '$NAMESPACE'..."
        kubectl create namespace "$NAMESPACE"
        print_success "Namespace '$NAMESPACE' created"
    else
        print_success "Namespace '$NAMESPACE' already exists"
    fi
}

# Function to validate chart
validate_chart() {
    print_status "Validating Helm chart..."
    
    if [ ! -d "$CHART_PATH" ]; then
        print_error "Chart path '$CHART_PATH' does not exist"
        exit 1
    fi
    
    if ! helm lint "$CHART_PATH"; then
        print_error "Helm chart validation failed"
        exit 1
    fi
    
    print_success "Helm chart validation passed"
}

# Function to deploy the application
deploy_application() {
    print_status "Deploying GetHome application..."
    
    # Build helm command
    HELM_CMD="helm upgrade --install $RELEASE_NAME $CHART_PATH --namespace $NAMESPACE"
    
    # Add values file if specified
    if [ -n "$VALUES_FILE" ]; then
        HELM_CMD="$HELM_CMD -f $VALUES_FILE"
    elif [ "$ENVIRONMENT" = "development" ]; then
        HELM_CMD="$HELM_CMD -f $CHART_PATH/values-dev.yaml"
    fi
    
    # Add wait flag
    HELM_CMD="$HELM_CMD --wait --timeout=10m"
    
    print_status "Executing: $HELM_CMD"
    
    if eval "$HELM_CMD"; then
        print_success "Application deployed successfully"
    else
        print_error "Application deployment failed"
        exit 1
    fi
}

# Function to verify deployment
verify_deployment() {
    print_status "Verifying deployment..."
    
    # Wait for pods to be ready
    print_status "Waiting for pods to be ready..."
    kubectl wait --for=condition=ready pod -l app.kubernetes.io/instance="$RELEASE_NAME" -n "$NAMESPACE" --timeout=300s
    
    # Check pod status
    print_status "Checking pod status..."
    kubectl get pods -n "$NAMESPACE" -l app.kubernetes.io/instance="$RELEASE_NAME"
    
    # Check services
    print_status "Checking services..."
    kubectl get services -n "$NAMESPACE" -l app.kubernetes.io/instance="$RELEASE_NAME"
    
    # Check ingress
    print_status "Checking ingress..."
    kubectl get ingress -n "$NAMESPACE" -l app.kubernetes.io/instance="$RELEASE_NAME"
    
    print_success "Deployment verification completed"
}

# Function to show deployment info
show_deployment_info() {
    print_status "Deployment Information:"
    echo "  Namespace: $NAMESPACE"
    echo "  Release Name: $RELEASE_NAME"
    echo "  Environment: $ENVIRONMENT"
    echo "  Chart Path: $CHART_PATH"
    if [ -n "$VALUES_FILE" ]; then
        echo "  Values File: $VALUES_FILE"
    fi
    
    echo ""
    print_status "Useful commands:"
    echo "  kubectl get pods -n $NAMESPACE"
    echo "  kubectl get services -n $NAMESPACE"
    echo "  kubectl get ingress -n $NAMESPACE"
    echo "  helm status $RELEASE_NAME -n $NAMESPACE"
    echo "  helm uninstall $RELEASE_NAME -n $NAMESPACE"
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -n|--namespace)
            NAMESPACE="$2"
            shift 2
            ;;
        -r|--release)
            RELEASE_NAME="$2"
            shift 2
            ;;
        -e|--environment)
            ENVIRONMENT="$2"
            shift 2
            ;;
        -f|--values)
            VALUES_FILE="$2"
            shift 2
            ;;
        -h|--help)
            show_usage
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            show_usage
            exit 1
            ;;
    esac
done

# Validate environment
if [ "$ENVIRONMENT" != "production" ] && [ "$ENVIRONMENT" != "development" ]; then
    print_error "Invalid environment: $ENVIRONMENT. Must be 'production' or 'development'"
    exit 1
fi

# Main deployment process
echo "=========================================="
echo "GetHome Application Deployment"
echo "=========================================="
echo ""

check_prerequisites
create_namespace
validate_chart
deploy_application
verify_deployment

echo ""
echo "=========================================="
print_success "Deployment completed successfully!"
echo "=========================================="
echo ""

show_deployment_info 