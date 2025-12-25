#!/usr/bin/env bash

###############################################################################
# REMSFAL Backend - Azure Container Registry Deployment Script
###############################################################################
#
# This script builds the backend microservices, runs tests, creates Docker 
# images, and pushes them to Azure Container Registry (ACR).
#
# REQUIRES: Bash 4+ (for associative arrays)
# On macOS: brew install bash && /opt/homebrew/bin/bash deploy-to-acr.sh ...
#
# SERVICES:
#   - remsfal-platform     (Port 8080) - Main platform service with PostgreSQL
#   - remsfal-ticketing    (Port 8081) - Ticketing service with Cassandra/CosmosDB
#   - remsfal-notification (Port 8082) - Notification service (dummy mail provider)
#
# USAGE:
#   chmod +x .github/workflows/deploy-to-acr.sh
#   ./.github/workflows/deploy-to-acr.sh <ACR_NAME> <ACR_USERNAME> <ACR_PASSWORD> [IMAGE_TAG] [SERVICE]
#
# EXAMPLES:
#   # Deploy all services
#   ./.github/workflows/deploy-to-acr.sh rmsfldevweuacr rmsfldevweuacr "YOUR_PASSWORD" v1.0.0
#
#   # Deploy only platform service
#   ./.github/workflows/deploy-to-acr.sh rmsfldevweuacr rmsfldevweuacr "YOUR_PASSWORD" v1.0.0 platform
#
#   # Deploy only ticketing service
#   ./.github/workflows/deploy-to-acr.sh rmsfldevweuacr rmsfldevweuacr "YOUR_PASSWORD" latest ticketing
#
#   # Deploy only ticketing service
#   ./.github/workflows/deploy-to-acr.sh rmsfldevweuacr rmsfldevweuacr "YOUR_PASSWORD" latest all --skip-tests
#
# PULL AND RUN LOCALLY:
#   # 1. Login to ACR
#   docker login rmsfldevweuacr.azurecr.io -u rmsfldevweuacr -p "YOUR_PASSWORD"
#
#   # 2. Pull images
#   docker pull rmsfldevweuacr.azurecr.io/remsfal-platform:latest
#   docker pull rmsfldevweuacr.azurecr.io/remsfal-ticketing:latest
#   docker pull rmsfldevweuacr.azurecr.io/remsfal-notification:latest
#
#   # 3. Run containers (requires env variables - see CONTAINER APPS CONFIG below)
#   docker run -d -p 8080:8080 --name platform rmsfldevweuacr.azurecr.io/remsfal-platform:latest
#
# CONTAINER APPS CONFIGURATION:
#   The services require secrets from Azure Key Vault. Configure Container Apps
#   to inject these as environment variables (see main.tf container_app resource).
#
#   Required secrets per service:
#   - platform:     postgres-connection-string, eventhub-bootstrap-server, eventhub-connection-string
#   - ticketing:    cosmos-contact-point, cosmos-username, cosmos-password, 
#                   storage-connection-string, eventhub-bootstrap-server, eventhub-connection-string
#   - notification: eventhub-bootstrap-server, eventhub-connection-string
#
# PARAMETERS:
#   ACR_NAME      - Name of Azure Container Registry (e.g., rmsfldevweuacr)
#   ACR_USERNAME  - ACR username (typically same as ACR_NAME)
#   ACR_PASSWORD  - ACR password or access token
#   IMAGE_TAG     - (Optional) Docker image tag (default: latest)
#   SERVICE       - (Optional) Single service to deploy: platform|ticketing|notification|all
#   SKIP_TESTS    - (Optional) Set to "true" to skip tests
#
###############################################################################

set -e  # Exit on error
set -u  # Exit on undefined variable

# Color output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_step() {
    echo -e "${CYAN}[STEP]${NC} $1"
}

# Parse arguments - separate flags from positional args
SKIP_TESTS="false"
POSITIONAL_ARGS=()

for arg in "$@"; do
    case $arg in
        --skip-tests)
            SKIP_TESTS="true"
            ;;
        *)
            POSITIONAL_ARGS+=("$arg")
            ;;
    esac
done

# Validate input parameters
if [ ${#POSITIONAL_ARGS[@]} -lt 3 ]; then
    log_error "Missing required parameters"
    echo ""
    echo "Usage: $0 <ACR_NAME> <ACR_USERNAME> <ACR_PASSWORD> [IMAGE_TAG] [SERVICE] [--skip-tests]"
    echo ""
    echo "Examples:"
    echo "  $0 rmsfldevweuacr rmsfldevweuacr \"YOUR_PASSWORD\" v1.0.0"
    echo "  $0 rmsfldevweuacr rmsfldevweuacr \"YOUR_PASSWORD\" latest platform"
    echo "  $0 rmsfldevweuacr rmsfldevweuacr \"YOUR_PASSWORD\" latest all --skip-tests"
    echo ""
    echo "Services: platform, ticketing, notification, all (default)"
    echo "Flags:    --skip-tests  Skip running tests before build"
    exit 1
fi

ACR_NAME="${POSITIONAL_ARGS[0]}"
ACR_USERNAME="${POSITIONAL_ARGS[1]}"
ACR_PASSWORD="${POSITIONAL_ARGS[2]}"
IMAGE_TAG="${POSITIONAL_ARGS[3]:-latest}"
SERVICE_FILTER="${POSITIONAL_ARGS[4]:-all}"

# Configuration
ACR_LOGIN_SERVER="${ACR_NAME}.azurecr.io"
DOCKERFILE_PATH="src/main/docker/Dockerfile"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

# Services configuration (compatible with Bash 3.2)
get_service_name() {
    case "$1" in
        platform) echo "remsfal-platform" ;;
        ticketing) echo "remsfal-ticketing" ;;
        notification) echo "remsfal-notification" ;;
        *) echo "" ;;
    esac
}

get_service_port() {
    case "$1" in
        platform) echo "8080" ;;
        ticketing) echo "8081" ;;
        notification) echo "8082" ;;
        *) echo "" ;;
    esac
}

ALL_SERVICES="platform ticketing notification"

# Change to project root
cd "${PROJECT_ROOT}"

echo "======================================================================"
log_info "REMSFAL Backend Deployment to Azure Container Registry"
echo "======================================================================"
echo ""
log_info "ACR:         ${ACR_LOGIN_SERVER}"
log_info "Image Tag:   ${IMAGE_TAG}"
log_info "Service:     ${SERVICE_FILTER}"
log_info "Project:     ${PROJECT_ROOT}"
echo ""

# Determine which services to deploy
SERVICES_TO_DEPLOY=""
if [ "${SERVICE_FILTER}" = "all" ]; then
    SERVICES_TO_DEPLOY="${ALL_SERVICES}"
else
    # Check if service exists
    if [ -n "$(get_service_name "${SERVICE_FILTER}")" ]; then
        SERVICES_TO_DEPLOY="${SERVICE_FILTER}"
    else
        log_error "Unknown service: ${SERVICE_FILTER}"
        log_info "Available services: platform, ticketing, notification, all"
        exit 1
    fi
fi

log_info "Services to deploy: ${SERVICES_TO_DEPLOY}"
echo ""

###############################################################################
# Step 1: Check prerequisites
###############################################################################
log_step "Step 1: Checking prerequisites..."

if ! command -v java &> /dev/null; then
    log_error "Java is not installed. Please install JDK 21 first."
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "${JAVA_VERSION}" -lt 17 ]; then
    log_error "Java 17+ is required. Found: ${JAVA_VERSION}"
    exit 1
fi

if ! command -v mvn &> /dev/null && ! command -v ./mvnw &> /dev/null; then
    log_error "Maven is not installed and mvnw wrapper not found."
    exit 1
fi

if ! command -v docker &> /dev/null; then
    log_error "Docker is not installed. Please install Docker first."
    exit 1
fi

# Check if Docker daemon is running
if ! docker info &> /dev/null; then
    log_error "Docker daemon is not running. Please start Docker."
    exit 1
fi

log_success "All prerequisites are met (Java ${JAVA_VERSION}, Maven, Docker)"
echo ""

# Set Maven command (used by tests and build steps)
MVN_CMD="./mvnw"
if [ ! -f "./mvnw" ]; then
    MVN_CMD="mvn"
fi

###############################################################################
# Step 2: Run tests for selected services
###############################################################################
if [ "${SKIP_TESTS}" = "true" ]; then
    log_warning "Step 2: Skipping tests (SKIP_TESTS=true)"
else
    log_step "Step 2: Running tests for selected services..."

    for service_key in ${SERVICES_TO_DEPLOY}; do
        service_name=$(get_service_name "${service_key}")
        log_info "Running tests for ${service_name}..."
        
        ${MVN_CMD} test -pl "remsfal-services/${service_name}" -am -q \
            -Dquarkus.test.profile=test \
            -DskipITs=true \
            || { log_error "Tests failed for ${service_name}"; exit 1; }
        
        log_success "Tests passed for ${service_name}"
    done
fi

echo ""

###############################################################################
# Step 3: Build the services (package)
###############################################################################
log_step "Step 3: Building services..."

for service_key in ${SERVICES_TO_DEPLOY}; do
    service_name=$(get_service_name "${service_key}")
    log_info "Building ${service_name}..."
    
    ${MVN_CMD} package -pl "remsfal-services/${service_name}" -am -q \
        -DskipTests \
        -Dquarkus.package.jar.type=uber-jar \
        || { log_error "Build failed for ${service_name}"; exit 1; }
    
    # Verify JAR exists
    JAR_FILE="remsfal-services/${service_name}/target/${service_name}-runner.jar"
    if [ ! -f "${JAR_FILE}" ]; then
        # Check for alternate naming
        JAR_FILE="remsfal-services/${service_name}/target/${service_name}.jar"
        if [ ! -f "${JAR_FILE}" ]; then
            log_error "JAR file not found for ${service_name}"
            log_info "Expected: remsfal-services/${service_name}/target/${service_name}-runner.jar"
            exit 1
        fi
    fi
    
    log_success "Built ${service_name} -> $(basename ${JAR_FILE})"
done

echo ""

###############################################################################
# Step 4: Build Docker images
###############################################################################
log_step "Step 4: Building Docker images for linux/amd64 platform..."

log_warning "Building for linux/amd64 (required for Azure Container Apps)"
echo ""

for service_key in ${SERVICES_TO_DEPLOY}; do
    service_name=$(get_service_name "${service_key}")
    full_image_name="${ACR_LOGIN_SERVER}/${service_name}:${IMAGE_TAG}"
    
    log_info "Building Docker image for ${service_name}..."
    
    docker build \
        --platform linux/amd64 \
        -f "${DOCKERFILE_PATH}" \
        --build-arg SERVICE_NAME="${service_name}" \
        -t "${full_image_name}" \
        . \
        || { log_error "Docker build failed for ${service_name}"; exit 1; }
    
    log_success "Built image: ${full_image_name}"
done

echo ""

###############################################################################
# Step 5: Login to ACR
###############################################################################
log_step "Step 5: Logging in to Azure Container Registry..."

echo "${ACR_PASSWORD}" | docker login "${ACR_LOGIN_SERVER}" \
    --username "${ACR_USERNAME}" \
    --password-stdin \
    || { log_error "Failed to login to ACR"; exit 1; }

log_success "Successfully logged in to ACR: ${ACR_LOGIN_SERVER}"
echo ""

###############################################################################
# Step 6: Push Docker images
###############################################################################
log_step "Step 6: Pushing Docker images to ACR..."

for service_key in ${SERVICES_TO_DEPLOY}; do
    service_name=$(get_service_name "${service_key}")
    full_image_name="${ACR_LOGIN_SERVER}/${service_name}:${IMAGE_TAG}"
    
    log_info "Pushing ${service_name}..."
    
    docker push "${full_image_name}" \
        || { log_error "Failed to push ${service_name}"; exit 1; }
    
    log_success "Pushed: ${full_image_name}"
done

echo ""

###############################################################################
# Summary
###############################################################################
echo "======================================================================"
log_success "DEPLOYMENT COMPLETED SUCCESSFULLY!"
echo "======================================================================"
echo ""
echo "Deployed Images:"
for service_key in ${SERVICES_TO_DEPLOY}; do
    service_name=$(get_service_name "${service_key}")
    port=$(get_service_port "${service_key}")
    echo "  - ${ACR_LOGIN_SERVER}/${service_name}:${IMAGE_TAG} (Port ${port})"
done
echo ""
echo "Next Steps:"
echo "  1. Update Container Apps to use new images"
echo "  2. Configure Key Vault secrets as environment variables"
echo "  3. Verify health endpoints after deployment"
echo ""
echo "Container Apps Update Commands:"
for service_key in ${SERVICES_TO_DEPLOY}; do
    service_name=$(get_service_name "${service_key}")
    echo "  az containerapp update --name <app-name> --resource-group <rg> \\"
    echo "    --image ${ACR_LOGIN_SERVER}/${service_name}:${IMAGE_TAG}"
done
echo ""
echo "======================================================================"
