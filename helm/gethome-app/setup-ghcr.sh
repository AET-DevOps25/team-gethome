#!/bin/bash

# GetHome GitHub Container Registry Setup Script
# This script helps you set up and deploy the GetHome application using GHCR

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
REPO_NAME="aet-devops25/team-gethome"
NAMESPACE="devops25-k8s-gethome"
REGISTRY="ghcr.io"

echo -e "${BLUE}================================${NC}"
echo -e "${BLUE}  GetHome GHCR Setup Script${NC}"
echo -e "${BLUE}================================${NC}"

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check prerequisites
check_prerequisites() {
    print_status "Checking prerequisites..."
    
    # Check if kubectl is installed
    if ! command -v kubectl &> /dev/null; then
        print_error "kubectl is not installed. Please install kubectl first."
        exit 1
    fi
    
    # Check if helm is installed
    if ! command -v helm &> /dev/null; then
        print_error "helm is not installed. Please install helm first."
        exit 1
    fi
    
    # Check if docker is installed
    if ! command -v docker &> /dev/null; then
        print_warning "docker is not installed. You won't be able to build images locally."
    fi
    
    print_status "Prerequisites check completed."
}

# Setup GitHub Container Registry
setup_ghcr() {
    print_status "Setting up GitHub Container Registry..."
    
    echo -e "${BLUE}GitHub Container Registry Setup:${NC}"
    echo "1. Your images will be available at: ${REGISTRY}/${REPO_NAME}/"
    echo "2. Images will be built automatically via GitHub Actions"
    echo "3. No authentication required for public repositories"
    echo ""
    
    read -p "Do you want to configure GHCR authentication for private repos? (y/N): " -n 1 -r
    echo
    
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        print_status "Setting up GHCR authentication..."
        
        # Check if GITHUB_TOKEN is set
        if [ -z "$GITHUB_TOKEN" ]; then
            print_error "GITHUB_TOKEN environment variable is not set."
            echo "Please set it with: export GITHUB_TOKEN=your_github_token"
            exit 1
        fi
        
        # Login to GHCR
        echo "$GITHUB_TOKEN" | docker login ghcr.io -u "$GITHUB_USERNAME" --password-stdin
        print_status "Successfully logged in to GHCR"
    else
        print_status "Skipping GHCR authentication (using public access)"
    fi
}

# Create Kubernetes namespace
create_namespace() {
    print_status "Creating Kubernetes namespace..."
    
    kubectl create namespace "$NAMESPACE" --dry-run=client -o yaml | kubectl apply -f -
    print_status "Namespace '$NAMESPACE' created/updated"
}

# Deploy the application
deploy_application() {
    print_status "Deploying GetHome application..."
    
    echo -e "${BLUE}Deployment Options:${NC}"
    echo "1. Development (uses develop tags)"
    echo "2. Production (uses latest tags)"
    echo "3. Custom (specify image tags)"
    echo ""
    
    read -p "Choose deployment type (1-3): " -n 1 -r
    echo
    
    case $REPLY in
        1)
            print_status "Deploying to development environment..."
            helm upgrade --install gethome-app ./helm/gethome-app \
                --namespace "$NAMESPACE" \
                --create-namespace \
                -f ./helm/gethome-app/values-dev.yaml \
                --set global.environment=development \
                --set global.imageRegistry="$REGISTRY" \
                --wait --timeout=10m
            ;;
        2)
            print_status "Deploying to production environment..."
            helm upgrade --install gethome-app ./helm/gethome-app \
                --namespace "$NAMESPACE" \
                --create-namespace \
                -f ./helm/gethome-app/values.yaml \
                --set global.environment=production \
                --set global.imageRegistry="$REGISTRY" \
                --wait --timeout=15m
            ;;
        3)
            print_status "Custom deployment..."
            read -p "Enter image tag (e.g., v1.0.0, main-abc1234): " IMAGE_TAG
            
            helm upgrade --install gethome-app ./helm/gethome-app \
                --namespace "$NAMESPACE" \
                --create-namespace \
                -f ./helm/gethome-app/values.yaml \
                --set global.environment=custom \
                --set global.imageRegistry="$REGISTRY" \
                --set services.auth-service.image="$REGISTRY/$REPO_NAME/auth-service:$IMAGE_TAG" \
                --set services.usermanagement-service.image="$REGISTRY/$REPO_NAME/usermanagement-service:$IMAGE_TAG" \
                --set services.ai-service.image="$REGISTRY/$REPO_NAME/ai-service:$IMAGE_TAG" \
                --set services.message-service.image="$REGISTRY/$REPO_NAME/message-service:$IMAGE_TAG" \
                --set services.routing-service.image="$REGISTRY/$REPO_NAME/routing-service:$IMAGE_TAG" \
                --set services.react-client.image="$REGISTRY/$REPO_NAME/react-client:$IMAGE_TAG" \
                --wait --timeout=15m
            ;;
        *)
            print_error "Invalid option. Exiting."
            exit 1
            ;;
    esac
    
    print_status "Deployment completed successfully!"
}

# Verify deployment
verify_deployment() {
    print_status "Verifying deployment..."
    
    # Wait for pods to be ready
    kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=gethome-app \
        --namespace "$NAMESPACE" --timeout=300s
    
    # Show deployment status
    echo -e "${BLUE}Deployment Status:${NC}"
    kubectl get pods -n "$NAMESPACE"
    
    echo -e "${BLUE}Services:${NC}"
    kubectl get services -n "$NAMESPACE"
    
    echo -e "${BLUE}Ingress:${NC}"
    kubectl get ingress -n "$NAMESPACE"
    
    print_status "Verification completed!"
}

# Show access information
show_access_info() {
    print_status "Access Information:"
    
    echo -e "${BLUE}Application Access:${NC}"
    echo "Frontend: http://gethome-dev.local (development)"
    echo "Frontend: https://gethome.local (production)"
    
    echo -e "${BLUE}API Endpoints:${NC}"
    echo "Auth Service: /api/auth"
    echo "User Management: /api/user"
    echo "AI Service: /api/ai"
    echo "message Service: /api/message"
    echo "Routing Service: /api/routing"
    
    echo -e "${BLUE}Monitoring:${NC}"
    echo "Grafana: /monitoring"
    echo "Prometheus: /prometheus"
    
    echo -e "${BLUE}Container Images:${NC}"
    echo "Registry: $REGISTRY/$REPO_NAME/"
    echo "Available images:"
    echo "  - auth-service"
    echo "  - usermanagement-service"
    echo "  - ai-service"
    echo "  - message-service"
    echo "  - routing-service"
    echo "  - react-client"
}

# Cleanup function
cleanup() {
    print_status "Cleaning up..."
    
    read -p "Do you want to uninstall the application? (y/N): " -n 1 -r
    echo
    
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        helm uninstall gethome-app -n "$NAMESPACE"
        print_status "Application uninstalled"
        
        read -p "Do you want to delete the namespace? (y/N): " -n 1 -r
        echo
        
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            kubectl delete namespace "$NAMESPACE"
            print_status "Namespace deleted"
        fi
    fi
}

# Main menu
show_menu() {
    echo -e "${BLUE}GetHome GHCR Setup Menu:${NC}"
    echo "1. Check prerequisites"
    echo "2. Setup GitHub Container Registry"
    echo "3. Create namespace"
    echo "4. Deploy application"
    echo "5. Verify deployment"
    echo "6. Show access information"
    echo "7. Cleanup"
    echo "8. Run full setup (1-6)"
    echo "9. Exit"
    echo ""
}

# Main execution
main() {
    if [ $# -eq 0 ]; then
        # Interactive mode
        while true; do
            show_menu
            read -p "Choose an option (1-9): " -n 1 -r
            echo
            echo
            
            case $REPLY in
                1) check_prerequisites ;;
                2) setup_ghcr ;;
                3) create_namespace ;;
                4) deploy_application ;;
                5) verify_deployment ;;
                6) show_access_info ;;
                7) cleanup ;;
                8)
                    check_prerequisites
                    setup_ghcr
                    create_namespace
                    deploy_application
                    verify_deployment
                    show_access_info
                    ;;
                9) 
                    print_status "Exiting..."
                    exit 0
                    ;;
                *) 
                    print_error "Invalid option. Please try again."
                    ;;
            esac
            
            echo ""
            read -p "Press Enter to continue..."
            echo ""
        done
    else
        # Command line mode
        case $1 in
            "check") check_prerequisites ;;
            "setup") setup_ghcr ;;
            "deploy") 
                create_namespace
                deploy_application
                verify_deployment
                ;;
            "verify") verify_deployment ;;
            "cleanup") cleanup ;;
            "full")
                check_prerequisites
                setup_ghcr
                create_namespace
                deploy_application
                verify_deployment
                show_access_info
                ;;
            *)
                echo "Usage: $0 [check|setup|deploy|verify|cleanup|full]"
                echo "  check   - Check prerequisites"
                echo "  setup   - Setup GHCR"
                echo "  deploy  - Deploy application"
                echo "  verify  - Verify deployment"
                echo "  cleanup - Cleanup resources"
                echo "  full    - Run full setup"
                exit 1
                ;;
        esac
    fi
}

# Run main function
main "$@" 