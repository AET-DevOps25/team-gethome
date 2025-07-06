# GetHome Integration Guide

This guide explains how all GetHome services work together to create a complete, functional safety application.

## 🏗️ System Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   React Client  │    │   Auth Service   │    │ User Management │
│   (Frontend)    │◄──►│   (Port 8081)    │◄──►│   (Port 8082)   │
└─────────────────┘    └──────────────────┘    └─────────────────┘
         │                       │                       │
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│ Routing Service │    │ Message Service  │    │   AI Service    │
│  (Port 8084)    │◄──►│   (Port 8083)    │    │   (Port 8085)   │
└─────────────────┘    └──────────────────┘    └─────────────────┘
         │                       │
         │                       │
         ▼                       ▼
┌─────────────────┐    ┌──────────────────┐
│   MongoDB       │    │   External APIs  │
│   (Database)    │    │ (Twilio, OpenAI) │
└─────────────────┘    └──────────────────┘
```

## 🔄 Service Communication Flow

### 1. User Authentication Flow
```
React Client → Auth Service → User Management Service
     ↓              ↓                    ↓
JWT Token ← User Profile ← Emergency Contacts
```

**Steps:**
1. User registers/logs in through React client
2. Auth Service validates credentials and issues JWT token
3. React client stores JWT token in localStorage
4. All subsequent API calls include JWT token in Authorization header

### 2. User Profile Management Flow
```
React Client → User Management Service → Auth Service
     ↓                    ↓                    ↓
Profile Form ← Profile Data ← User Validation
```

**Steps:**
1. User completes profile after authentication
2. Profile data is stored in User Management Service
3. Profile data is used to personalize AI interactions
4. Emergency contacts are managed through User Management Service

### 3. Route Planning Flow
```
React Client → Routing Service → OpenRouteService API
     ↓              ↓                    ↓
Map Interface ← Safe Route ← Danger Zone Data
```

**Steps:**
1. User sets destination on map
2. Routing Service calculates route using OpenRouteService
3. Danger zones are checked and avoided
4. Safe route is displayed on map with turn-by-turn instructions

### 4. Emergency Response Flow
```
User Trigger → Routing Service → Message Service → Emergency Contacts
     ↓              ↓                    ↓                    ↓
Emergency ← Location Data ← Notification ← Email/SMS
```

**Steps:**
1. User triggers emergency (manual or audio-based)
2. Routing Service processes emergency and gets user's location
3. User's emergency contacts are retrieved from User Management Service
4. Message Service sends notifications to all emergency contacts
5. Notifications include location, context, and user information

### 5. AI Companion Flow
```
React Client → AI Service → User Management Service
     ↓              ↓                    ↓
Chat Interface ← Personalized Response ← User Profile
```

**Steps:**
1. User starts chat session
2. AI Service fetches user profile for personalization
3. AI provides companionship based on user preferences
4. AI can detect emergency situations through chat analysis
5. If emergency detected, automatic notification is sent

## 🔌 API Integration Points

### Service-to-Service Communication

#### Auth Service (Port 8081)
- **Purpose:** User authentication and JWT token management
- **Key Endpoints:**
  - `POST /api/v1/auth/register` - User registration
  - `POST /api/v1/auth/login` - User login
  - `GET /api/v1/auth/me` - Get current user
- **Integration:** All other services validate JWT tokens from Auth Service

#### User Management Service (Port 8082)
- **Purpose:** User profiles and emergency contacts
- **Key Endpoints:**
  - `GET /api/users/{userId}/profile` - Get user profile
  - `PUT /api/users/{userId}/profile` - Update user profile
  - `GET /api/users/{userId}/emergency-contacts` - Get emergency contacts
- **Integration:** 
  - Auth Service validates user existence
  - Message Service gets emergency contacts for notifications
  - AI Service gets user profile for personalization

#### Routing Service (Port 8084)
- **Purpose:** Safe route planning and emergency handling
- **Key Endpoints:**
  - `POST /api/routes/plan` - Plan safe route
  - `POST /api/emergency/trigger` - Trigger emergency
  - `POST /api/danger-zones/report` - Report danger zone
- **Integration:**
  - Calls Message Service for emergency notifications
  - Calls User Management Service for emergency contacts
  - Uses OpenRouteService API for route calculation

#### Message Service (Port 8083)
- **Purpose:** Emergency notifications via email and SMS
- **Key Endpoints:**
  - `POST /api/emergency/notify` - Send emergency notification
  - `POST /api/messages/email/send` - Send email
  - `POST /api/messages/sms/send` - Send SMS
- **Integration:**
  - Called by Routing Service for emergency notifications
  - Uses Twilio for SMS delivery
  - Uses SMTP for email delivery

#### AI Service (Port 8085)
- **Purpose:** AI companion and emergency detection
- **Key Endpoints:**
  - `POST /api/chat/sessions` - Start chat session
  - `POST /api/chat/sessions/{sessionId}/message` - Send message
- **Integration:**
  - Calls User Management Service for user profile
  - Can trigger emergency notifications through Routing Service

## 🗄️ Database Integration

### MongoDB Collections

#### Auth Service Database (`gethome_auth`)
- `users` - User accounts and authentication data
- `roles` - User roles and permissions

#### User Management Service Database (`gethome_users`)
- `auth_users` - User profiles and preferences
- `emergency_contacts` - Emergency contact relationships
- `user_profiles` - Detailed user profile information

#### Message Service Database (`gethome_messages`)
- `emergency_notifications` - Emergency notification records
- `message_logs` - Message delivery logs
- `message_templates` - Email and SMS templates

#### Routing Service Database (`gethome_routing`)
- `routes` - User route history
- `danger_zones` - Community-reported danger zones

## 🔐 Security Integration

### JWT Token Flow
1. **Token Generation:** Auth Service generates JWT tokens on successful login
2. **Token Validation:** All services validate JWT tokens using shared secret
3. **Token Storage:** React client stores tokens in localStorage
4. **Token Transmission:** All API requests include token in Authorization header

### Service-to-Service Authentication
- Services use shared JWT secret for inter-service communication
- Feign clients automatically include authentication headers
- Service mesh provides additional security layer in Kubernetes

## 📊 Monitoring Integration

### Health Checks
- All services expose `/actuator/health` endpoints
- Kubernetes uses health checks for pod readiness
- Prometheus scrapes metrics from all services

### Metrics Collection
- **Auth Service:** Login attempts, registration rates
- **User Management Service:** Profile updates, contact additions
- **Routing Service:** Route requests, danger zone reports
- **Message Service:** Notification delivery rates
- **AI Service:** Chat sessions, message processing

### Alerting
- AlertManager routes alerts based on severity
- Critical alerts sent to emergency channels
- Performance alerts sent to development team

## 🚀 Deployment Integration

### Container Orchestration
- All services deployed as Kubernetes pods
- Helm manages deployment configuration
- Services communicate via Kubernetes service names

### Environment Configuration
- Environment variables configured through Helm values
- Secrets managed through Kubernetes secrets
- Service URLs configured for inter-service communication

### Scaling
- Horizontal Pod Autoscaler scales services based on load
- Each service can scale independently
- Load balancers distribute traffic across service instances

## 🧪 Testing Integration

### Integration Test Flow
1. **Health Checks:** Verify all services are running
2. **Authentication:** Test user registration and login
3. **Profile Management:** Test user profile creation and updates
4. **Emergency Contacts:** Test emergency contact management
5. **Route Planning:** Test safe route calculation
6. **Emergency Trigger:** Test emergency notification flow
7. **AI Chat:** Test AI companion functionality

### Test Data Flow
```
Test Script → Auth Service → User Management → Routing Service
     ↓              ↓              ↓              ↓
Test Results ← JWT Token ← User Profile ← Emergency Trigger
```

## 🔧 Configuration Integration

### Environment Variables
Each service requires specific environment variables for integration:

#### Auth Service
```bash
JWT_SECRET=shared-secret-key
JWT_EXPIRATION=86400000
SPRING_DATA_MONGODB_HOST=mongo
```

#### User Management Service
```bash
JWT_SECRET=shared-secret-key
SERVICE_AUTH_URL=http://auth-service:8081
SPRING_DATA_MONGODB_HOST=mongo
```

#### Routing Service
```bash
JWT_SECRET=shared-secret-key
SERVICE_USERMANAGEMENT_URL=http://usermanagement-service:8082
SERVICE_MESSAGE_URL=http://message-service:8083
ROUTING_API_KEY=openrouteservice-api-key
```

#### Message Service
```bash
JWT_SECRET=shared-secret-key
SERVICE_USERMANAGEMENT_URL=http://usermanagement-service:8082
TWILIO_ACCOUNT_SID=twilio-account-sid
TWILIO_AUTH_TOKEN=twilio-auth-token
```

#### AI Service
```bash
JWT_SECRET=shared-secret-key
USER_SERVICE_URL=http://usermanagement-service:8082/api/users/{user_id}/profile
OPENAI_API_KEY=openai-api-key
```

## 🎯 Key Integration Points

### 1. Emergency Response Chain
```
User Emergency → Routing Service → Message Service → Emergency Contacts
```

### 2. User Profile Chain
```
User Registration → Auth Service → User Management → AI Personalization
```

### 3. Route Safety Chain
```
Route Request → Routing Service → Danger Zone Check → Safe Route
```

### 4. AI Companion Chain
```
Chat Message → AI Service → User Profile → Personalized Response
```

## 🔍 Troubleshooting Integration Issues

### Common Issues and Solutions

1. **Service Communication Failures**
   - Check service URLs in environment variables
   - Verify Kubernetes service names
   - Check network policies

2. **Authentication Failures**
   - Verify JWT secret is shared across services
   - Check token expiration settings
   - Validate token format in requests

3. **Database Connection Issues**
   - Verify MongoDB host and port settings
   - Check database credentials
   - Validate database names

4. **External API Failures**
   - Check API keys in secrets
   - Verify API rate limits
   - Test external service connectivity

### Debugging Commands
```bash
# Check service health
kubectl get pods -n devops25-k8s-gethome

# View service logs
kubectl logs -f deployment/auth-service -n devops25-k8s-gethome

# Test service connectivity
kubectl port-forward svc/auth-service 8081:8081 -n devops25-k8s-gethome

# Check service endpoints
kubectl get endpoints -n devops25-k8s-gethome
```

This integration guide ensures that all GetHome services work together seamlessly to provide a complete safety application experience. 