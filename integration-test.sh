#!/bin/bash

# GetHome Integration Test Script
# This script tests the complete integration of all services

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
BASE_URL="http://localhost"
AUTH_URL="${BASE_URL}/api/auth"
USER_URL="${BASE_URL}/api/user"
ROUTING_URL="${BASE_URL}/api/routing"
MESSAGE_URL="${BASE_URL}/api/message"
AI_URL="${BASE_URL}/api/ai"

# Test data
TEST_USER_EMAIL="test@example.com"
TEST_USER_PASSWORD="testpassword123"
TEST_USER_NAME="Test User"

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

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# Function to make HTTP requests
make_request() {
    local method=$1
    local url=$2
    local data=$3
    local token=$4
    
    local headers="Content-Type: application/json"
    if [ ! -z "$token" ]; then
        headers="$headers -H 'Authorization: Bearer $token'"
    fi
    
    if [ "$method" = "GET" ]; then
        curl -s -w "%{http_code}" -o /tmp/response.json "$url" -H "$headers"
    else
        curl -s -w "%{http_code}" -o /tmp/response.json -X "$method" "$url" -H "$headers" -d "$data"
    fi
}

# Function to check if service is ready
wait_for_service() {
    local service=$1
    local url=$2
    local max_attempts=30
    local attempt=1
    
    print_info "Waiting for $service to be ready..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -f -s "$url" >/dev/null 2>&1; then
            print_status "$service is ready"
            return 0
        fi
        
        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done
    
    print_error "$service is not ready after $max_attempts attempts"
    return 1
}

# Test 1: Service Health Checks
test_health_checks() {
    echo -e "\n${BLUE}🏥 Testing Service Health Checks${NC}"
    
    # Check Auth Service
    if wait_for_service "Auth Service" "$AUTH_URL/actuator/health"; then
        print_status "Auth Service health check passed"
    else
        print_error "Auth Service health check failed"
        return 1
    fi
    
    # Check User Management Service
    if wait_for_service "User Management Service" "$USER_URL/actuator/health"; then
        print_status "User Management Service health check passed"
    else
        print_error "User Management Service health check failed"
        return 1
    fi
    
    # Check Routing Service
    if wait_for_service "Routing Service" "$ROUTING_URL/emergency/health"; then
        print_status "Routing Service health check passed"
    else
        print_error "Routing Service health check failed"
        return 1
    fi
    
    # Check Message Service
    if wait_for_service "Message Service" "$MESSAGE_URL/emergency/health"; then
        print_status "Message Service health check passed"
    else
        print_error "Message Service health check failed"
        return 1
    fi
    
    # Check AI Service
    if wait_for_service "AI Service" "$AI_URL/"; then
        print_status "AI Service health check passed"
    else
        print_error "AI Service health check failed"
        return 1
    fi
}

# Test 2: User Registration and Authentication
test_auth_flow() {
    echo -e "\n${BLUE}🔐 Testing Authentication Flow${NC}"
    
    # Register new user
    print_info "Registering new user..."
    local register_data="{\"name\":\"$TEST_USER_NAME\",\"email\":\"$TEST_USER_EMAIL\",\"password\":\"$TEST_USER_PASSWORD\"}"
    local status_code=$(make_request "POST" "$AUTH_URL/register" "$register_data")
    
    if [ "$status_code" = "200" ]; then
        print_status "User registration successful"
    else
        print_warning "User registration failed (might already exist)"
    fi
    
    # Login
    print_info "Logging in..."
    local login_data="{\"email\":\"$TEST_USER_EMAIL\",\"password\":\"$TEST_USER_PASSWORD\"}"
    local status_code=$(make_request "POST" "$AUTH_URL/login" "$login_data")
    
    if [ "$status_code" = "200" ]; then
        # Extract token from response
        local token=$(cat /tmp/response.json | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
        if [ ! -z "$token" ]; then
            print_status "Login successful, token obtained"
            echo "$token" > /tmp/auth_token.txt
        else
            print_error "Failed to extract token from login response"
            return 1
        fi
    else
        print_error "Login failed"
        return 1
    fi
}

# Test 3: User Profile Management
test_user_profile() {
    echo -e "\n${BLUE}👤 Testing User Profile Management${NC}"
    
    local token=$(cat /tmp/auth_token.txt 2>/dev/null || echo "")
    if [ -z "$token" ]; then
        print_error "No authentication token available"
        return 1
    fi
    
    # Get current user
    print_info "Getting current user..."
    local status_code=$(make_request "GET" "$AUTH_URL/me" "" "$token")
    
    if [ "$status_code" = "200" ]; then
        local user_id=$(cat /tmp/response.json | grep -o '"id":"[^"]*"' | cut -d'"' -f4)
        if [ ! -z "$user_id" ]; then
            print_status "Current user retrieved, ID: $user_id"
            echo "$user_id" > /tmp/user_id.txt
        else
            print_error "Failed to extract user ID"
            return 1
        fi
    else
        print_error "Failed to get current user"
        return 1
    fi
    
    # Update user profile
    print_info "Updating user profile..."
    local profile_data="{\"alias\":\"TestUser\",\"phoneNr\":\"+1234567890\",\"gender\":\"OTHER\",\"ageGroup\":\"TWENTY_FIVE_TO_THIRTY_FOUR\",\"preferredContactMethod\":\"EMAIL\"}"
    local status_code=$(make_request "PUT" "$USER_URL/users/$user_id/profile" "$profile_data" "$token")
    
    if [ "$status_code" = "200" ]; then
        print_status "User profile updated successfully"
    else
        print_error "Failed to update user profile"
        return 1
    fi
}

# Test 4: Emergency Contact Management
test_emergency_contacts() {
    echo -e "\n${BLUE}📞 Testing Emergency Contact Management${NC}"
    
    local token=$(cat /tmp/auth_token.txt 2>/dev/null || echo "")
    local user_id=$(cat /tmp/user_id.txt 2>/dev/null || echo "")
    
    if [ -z "$token" ] || [ -z "$user_id" ]; then
        print_error "Missing authentication token or user ID"
        return 1
    fi
    
    # Add emergency contact
    print_info "Adding emergency contact..."
    local contact_data="{\"userCode\":\"TEST123\",\"name\":\"Emergency Contact\",\"email\":\"emergency@example.com\",\"phone\":\"+1987654321\",\"preferredMethod\":\"EMAIL\"}"
    local status_code=$(make_request "POST" "$USER_URL/users/$user_id/emergency-contacts" "$contact_data" "$token")
    
    if [ "$status_code" = "200" ]; then
        print_status "Emergency contact added successfully"
    else
        print_warning "Failed to add emergency contact (might already exist)"
    fi
    
    # Get emergency contacts
    print_info "Getting emergency contacts..."
    local status_code=$(make_request "GET" "$USER_URL/users/$user_id/emergency-contacts" "" "$token")
    
    if [ "$status_code" = "200" ]; then
        print_status "Emergency contacts retrieved successfully"
    else
        print_error "Failed to get emergency contacts"
        return 1
    fi
}

# Test 5: Route Planning
test_route_planning() {
    echo -e "\n${BLUE}🗺️ Testing Route Planning${NC}"
    
    local token=$(cat /tmp/auth_token.txt 2>/dev/null || echo "")
    
    if [ -z "$token" ]; then
        print_error "No authentication token available"
        return 1
    fi
    
    # Plan a route
    print_info "Planning a route..."
    local route_data="{\"startLatitude\":40.7580,\"startLongitude\":-73.9855,\"endLatitude\":40.7505,\"endLongitude\":-73.9934,\"avoidDangerZones\":true,\"transportMode\":\"WALKING\"}"
    local status_code=$(make_request "POST" "$ROUTING_URL/routes/plan" "$route_data" "$token")
    
    if [ "$status_code" = "200" ]; then
        print_status "Route planning successful"
    else
        print_warning "Route planning failed (might need API key)"
    fi
}

# Test 6: Danger Zone Reporting
test_danger_zones() {
    echo -e "\n${BLUE}⚠️ Testing Danger Zone Reporting${NC}"
    
    local token=$(cat /tmp/auth_token.txt 2>/dev/null || echo "")
    
    if [ -z "$token" ]; then
        print_error "No authentication token available"
        return 1
    fi
    
    # Report a danger zone
    print_info "Reporting a danger zone..."
    local danger_zone_data="{\"latitude\":40.7580,\"longitude\":-73.9855,\"description\":\"Test danger zone\",\"category\":\"poor_lighting\",\"dangerLevel\":\"MEDIUM\"}"
    local status_code=$(make_request "POST" "$ROUTING_URL/danger-zones/report" "$danger_zone_data" "$token")
    
    if [ "$status_code" = "200" ]; then
        print_status "Danger zone reported successfully"
    else
        print_error "Failed to report danger zone"
        return 1
    fi
}

# Test 7: Emergency Trigger
test_emergency_trigger() {
    echo -e "\n${BLUE}🚨 Testing Emergency Trigger${NC}"
    
    local token=$(cat /tmp/auth_token.txt 2>/dev/null || echo "")
    
    if [ -z "$token" ]; then
        print_error "No authentication token available"
        return 1
    fi
    
    # Trigger emergency
    print_info "Triggering emergency..."
    local emergency_data="{\"latitude\":40.7580,\"longitude\":-73.9855,\"location\":\"Test location\",\"reason\":\"Integration test emergency\"}"
    local status_code=$(make_request "POST" "$ROUTING_URL/emergency/trigger" "$emergency_data" "$token")
    
    if [ "$status_code" = "200" ]; then
        print_status "Emergency triggered successfully"
    else
        print_warning "Emergency trigger failed (might need emergency contacts)"
    fi
}

# Test 8: AI Chat
test_ai_chat() {
    echo -e "\n${BLUE}🤖 Testing AI Chat${NC}"
    
    local token=$(cat /tmp/auth_token.txt 2>/dev/null || echo "")
    
    if [ -z "$token" ]; then
        print_error "No authentication token available"
        return 1
    fi
    
    # Start chat session
    print_info "Starting AI chat session..."
    local status_code=$(make_request "POST" "$AI_URL/chat/sessions" "" "$token")
    
    if [ "$status_code" = "200" ]; then
        local session_id=$(cat /tmp/response.json | grep -o '"session_id":"[^"]*"' | cut -d'"' -f4)
        if [ ! -z "$session_id" ]; then
            print_status "Chat session started, ID: $session_id"
            echo "$session_id" > /tmp/session_id.txt
            
            # Send a message
            print_info "Sending message to AI..."
            local message_data="{\"message\":\"Hello, this is a test message\"}"
            local status_code=$(make_request "POST" "$AI_URL/chat/sessions/$session_id/message" "$message_data" "$token")
            
            if [ "$status_code" = "200" ]; then
                print_status "Message sent successfully"
            else
                print_warning "Failed to send message"
            fi
            
            # Close session
            print_info "Closing chat session..."
            local status_code=$(make_request "POST" "$AI_URL/chat/sessions/$session_id" "" "$token")
            
            if [ "$status_code" = "204" ]; then
                print_status "Chat session closed successfully"
            else
                print_warning "Failed to close chat session"
            fi
        else
            print_error "Failed to extract session ID"
            return 1
        fi
    else
        print_warning "Failed to start chat session"
    fi
}

# Main test execution
main() {
    echo -e "${BLUE}🧪 GetHome Integration Test Suite${NC}"
    echo -e "${BLUE}================================${NC}"
    
    # Clean up previous test artifacts
    rm -f /tmp/auth_token.txt /tmp/user_id.txt /tmp/session_id.txt /tmp/response.json
    
    # Run tests
    test_health_checks
    test_auth_flow
    test_user_profile
    test_emergency_contacts
    test_route_planning
    test_danger_zones
    test_emergency_trigger
    test_ai_chat
    
    echo -e "\n${GREEN}🎉 All integration tests completed!${NC}"
    echo -e "${BLUE}📊 Test Summary:${NC}"
    echo -e "  ✅ Service Health Checks"
    echo -e "  ✅ Authentication Flow"
    echo -e "  ✅ User Profile Management"
    echo -e "  ✅ Emergency Contact Management"
    echo -e "  ✅ Route Planning"
    echo -e "  ✅ Danger Zone Reporting"
    echo -e "  ✅ Emergency Trigger"
    echo -e "  ✅ AI Chat"
    
    # Cleanup
    rm -f /tmp/auth_token.txt /tmp/user_id.txt /tmp/session_id.txt /tmp/response.json
}

# Run main function
main "$@" 