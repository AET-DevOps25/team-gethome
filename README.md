# GetHome - Safe Journey Companion

GetHome addresses two critical challenges faced by solo pedestrians, particularly in late‑night settings: **personal safety** and **emotional well‑being**. By combining community‑sourced safety data with an AI "companion," GetHome guides users along safer walking routes and provides real‑time support during their journey.

## 🏗️ System Architecture

### Microservices Architecture
GetHome follows a microservices architecture pattern with the following components:

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   React Client  │    │   Auth Service   │    │ User Management │
│   (Frontend)    │◄──►│   (Spring Boot)  │◄──►│   Service       │
└─────────────────┘    └──────────────────┘    │  (Spring Boot)  │
                                               └─────────────────┘
         │                                              │
         ▼                                              ▼
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│  Message Service│    │  Routing Service │    │   AI Service    │
│  (Spring Boot)  │◄──►│  (Spring Boot)   │◄──►│   (Python)      │
└─────────────────┘    └──────────────────┘    └─────────────────┘
         │                                              │
         ▼                                              ▼
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   MongoDB       │    │ Prometheus       │    │   Grafana       │
│   (Database)    │    │ (Metrics)        │    │ (Dashboards)    │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

### Technology Stack

#### Backend Services
- **Java 17** with **Spring Boot 3.x**
- **Python 3.11** for AI/ML services
- **MongoDB** for data persistence
- **JWT** for authentication
- **Gradle** for Java build management

#### Frontend
- **React 18** with **TypeScript**
- **Tailwind CSS** for styling
- **React Router** for navigation
- **Axios** for API communication

#### Infrastructure & Monitoring
- **Docker** & **Docker Compose** for containerization
- **Kubernetes** with **Helm** for orchestration
- **Prometheus** for metrics collection
- **Grafana** for visualization
- **AlertManager** for alerting
- **GitHub Actions** for CI/CD

## 🚀 Quick Start with Docker Compose

### Prerequisites
- Docker Desktop installed and running
- Docker Compose v2.0+
- At least 8GB RAM available for Docker
- Ports 3000, 8080-8084, 27017, 9090, 3001 available

### 1. Clone the Repository
```bash
git clone https://github.com/AET-DevOps25/team-gethome.git
cd team-gethome
```

### 2. Environment Setup
Create a `.env` file in the root directory:
```bash
# Copy the example environment file
cp deployment/env.example .env
```

Edit `.env` with your configuration:
```env
# MongoDB Configuration
MONGODB_URI=mongodb://mongo:27017/gethome
MONGO_ROOT_PASSWORD=your_secure_password

# JWT Configuration
JWT_SECRET=your_jwt_secret_key_here

# Email Configuration (for password reset)
EMAIL_USERNAME=your_email@gmail.com
EMAIL_PASSWORD=your_app_password

# Twilio Configuration (for SMS notifications)
TWILIO_ACCOUNT_SID=your_twilio_sid
TWILIO_AUTH_TOKEN=your_twilio_token
TWILIO_PHONE_NUMBER=your_twilio_phone

# AI Service Configuration
OPENAI_API_KEY=your_openai_api_key
OPENROUTE_API_KEY=your_openroute_api_key

# Spring Profiles
SPRING_PROFILES_ACTIVE=development
```

### 3. Start the Application
```bash
# Start all services
docker-compose up -d

# Or start with logs
docker-compose up

# To rebuild images
docker-compose up --build
```

### 4. Verify Services
```bash
# Check all containers are running
docker-compose ps

# View logs for a specific service
docker-compose logs -f auth-service
docker-compose logs -f react-client
```

### 5. Access the Application
- **Frontend**: http://localhost:3000
- **Auth Service**: http://localhost:8080
- **User Management**: http://localhost:8081
- **Message Service**: http://localhost:8082
- **Routing Service**: http://localhost:8083
- **AI Service**: http://localhost:8084
- **MongoDB**: localhost:27017
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3001 (admin/admin)

## 📋 Service Details

### 1. React Client (Frontend)
**Port**: 3000
**Technology**: React 18, TypeScript, Tailwind CSS

**Features**:
- User authentication and registration
- Interactive map interface
- Real-time route planning
- Safety alerts and notifications
- User profile management
- Emergency contact management

**Key Components**:
- `LandingPage`: Welcome and onboarding
- `LoginPage`/`RegisterPage`: Authentication
- `MapPage`: Interactive map with route planning
- `ChatPage`: AI companion interface
- `ProfilePage`: User settings and preferences
- `ReportsPage`: Safety incident reporting

### 2. Auth Service
**Port**: 8080
**Technology**: Spring Boot 3, Java 17

**Features**:
- User authentication (JWT)
- Password reset via email
- OAuth2 integration
- Session management
- Security configuration

**Endpoints**:
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration
- `POST /api/auth/forgot-password` - Password reset
- `POST /api/auth/reset-password` - Password reset confirmation
- `GET /api/auth/verify` - Token verification

### 3. User Management Service
**Port**: 8081
**Technology**: Spring Boot 3, Java 17

**Features**:
- User profile management
- Emergency contact management
- User preferences
- Profile picture generation
- User statistics and analytics

**Endpoints**:
- `GET /api/users/profile` - Get user profile
- `PUT /api/users/profile` - Update profile
- `POST /api/users/emergency-contacts` - Add emergency contact
- `GET /api/users/emergency-contacts` - List emergency contacts
- `DELETE /api/users/emergency-contacts/{id}` - Remove emergency contact

### 4. Message Service
**Port**: 8082
**Technology**: Spring Boot 3, Java 17

**Features**:
- Real-time messaging
- Emergency notifications
- SMS integration (Twilio)
- Email notifications
- Message templates

**Endpoints**:
- `POST /api/messages/send` - Send message
- `GET /api/messages/conversation/{id}` - Get conversation
- `POST /api/messages/emergency` - Send emergency notification
- `GET /api/messages/templates` - Get message templates

### 5. Routing Service
**Port**: 8083
**Technology**: Spring Boot 3, Java 17

**Features**:
- Route planning and optimization
- Safety-aware routing
- Danger zone detection
- Real-time traffic integration
- Route alternatives

**Endpoints**:
- `POST /api/routing/plan` - Plan route
- `GET /api/routing/danger-zones` - Get danger zones
- `POST /api/routing/danger-zones` - Report danger zone
- `GET /api/routing/alternatives` - Get route alternatives

### 6. AI Service
**Port**: 8084
**Technology**: Python 3.11, FastAPI

**Features**:
- AI companion functionality
- Natural language processing
- Safety recommendations
- Emotional support
- Route optimization suggestions

**Endpoints**:
- `POST /api/ai/chat` - AI conversation
- `POST /api/ai/analyze-route` - Route safety analysis
- `GET /api/ai/suggestions` - Safety suggestions
- `POST /api/ai/emergency-support` - Emergency AI support

### 7. GetHome Metrics Exporter
**Port**: 9091
**Technology**: Python 3.11, Prometheus Client

**Features**:
- Custom business metrics collection
- Safety analytics
- User engagement metrics
- System performance metrics
- Real-time monitoring data

**Metrics**:
- `gethome_estimated_incidents_prevented_total`
- `gethome_total_distance_saved_kilometers`
- `gethome_safety_score_average`
- `gethome_user_engagement_score`
- `gethome_emergency_response_efficiency_score`

## 🔧 Development Setup

### Prerequisites for Development
- Java 17 JDK
- Python 3.11
- Node.js 18+
- MongoDB 6.0+
- Gradle 8.0+

### Local Development
```bash
# Start MongoDB
docker run -d --name mongodb -p 27017:27017 mongo:6.0

# Start Java services (in separate terminals)
cd server/auth-service && ./gradlew bootRun
cd server/usermanagement-service && ./gradlew bootRun
cd server/message-service && ./gradlew bootRun
cd server/routing-service && ./gradlew bootRun

# Start Python services
cd server/ai-service && python -m uvicorn app.main:app --reload --port 8084
cd server/gethome-metrics-exporter && python app/main.py

# Start React client
cd client && npm install && npm start
```

### Testing
```bash
# Test Java services
cd server/auth-service && ./gradlew test
cd server/usermanagement-service && ./gradlew test
cd server/message-service && ./gradlew test
cd server/routing-service && ./gradlew test

# Test Python services
cd server/ai-service && python -m pytest tests/
cd server/gethome-metrics-exporter && python -m pytest tests/

# Test React client
cd client && npm test
```

## 📊 Monitoring & Observability

### Prometheus Metrics
The application exposes comprehensive metrics for monitoring:

**Business Metrics**:
- Safety incidents prevented
- Distance saved through route optimization
- User engagement scores
- Emergency response efficiency

**System Metrics**:
- API response times
- Error rates
- Resource utilization
- Service availability

### Grafana Dashboards
Four comprehensive dashboards provide insights:

1. **Business Intelligence**: Safety impact, user engagement, optimization effectiveness
2. **System Performance**: Service availability, API performance, resource utilization
3. **Security & Safety**: Danger zones, emergency responses, safety profiles
4. **Operational Insights**: Usage patterns, retention, operational efficiency

### Alerting
AlertManager provides notifications for:
- Service downtime
- High error rates
- Safety incidents
- Performance degradation
- Resource exhaustion

## 🚀 Deployment

### Kubernetes Deployment
```bash
# Deploy to Kubernetes
helm install gethome-app ./helm/gethome-app --namespace devops25-k8s-gethome

# Check deployment status
kubectl get pods -n devops25-k8s-gethome
kubectl get services -n devops25-k8s-gethome

# Access the application
kubectl port-forward svc/react-client 3000:80 -n devops25-k8s-gethome
```

### Production Considerations
- Use Kubernetes secrets for sensitive data
- Configure resource limits and requests
- Set up horizontal pod autoscaling
- Implement network policies
- Configure backup strategies
- Set up monitoring and alerting

## 🔒 Security Features

### Authentication & Authorization
- JWT-based authentication
- Role-based access control
- Password reset via email
- Session management
- OAuth2 integration

### Data Protection
- Encrypted data transmission (HTTPS)
- Secure password hashing
- Input validation and sanitization
- SQL injection prevention
- XSS protection

### Privacy
- GDPR compliance
- Data anonymization
- User consent management
- Data retention policies
- Privacy controls

## 🧪 Testing Strategy

### Unit Testing
- Java services: JUnit 5, Mockito
- Python services: pytest, unittest
- React client: Jest, React Testing Library

### Integration Testing
- API endpoint testing
- Database integration tests
- Service-to-service communication
- End-to-end workflows

### Performance Testing
- Load testing with JMeter
- Stress testing
- Performance benchmarking
- Resource utilization monitoring

## 📈 Performance & Scalability

### Performance Optimizations
- Database indexing
- Caching strategies
- Connection pooling
- Async processing
- CDN integration

### Scalability Features
- Horizontal scaling
- Load balancing
- Auto-scaling policies
- Database sharding
- Microservices architecture

## 🛠️ Troubleshooting

### Common Issues

**Docker Compose Issues**:
```bash
# Check container status
docker-compose ps

# View logs
docker-compose logs -f [service-name]

# Restart services
docker-compose restart [service-name]

# Rebuild and restart
docker-compose up --build -d
```

**Database Issues**:
```bash
# Connect to MongoDB
docker exec -it mongodb mongosh

# Check database status
docker exec -it mongodb mongosh --eval "db.stats()"
```

**Service Communication Issues**:
```bash
# Check service health
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8084/health
```

### Debug Commands
```bash
# Check network connectivity
docker network ls
docker network inspect team-gethome_default

# Check resource usage
docker stats

# View service logs
docker-compose logs -f --tail=100 [service-name]
```

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Support

### Getting Help
- Check the troubleshooting section
- Review the documentation
- Open an issue on GitHub
- Contact the development team

### Useful Links
- [Deployment Guide](https://github.com/AET-DevOps25/team-gethome/helm/gethome-app/QUICKSTART.md)
- [Monitoring Guide](https://github.com/AET-DevOps25/team-gethome/monitoring/ADVANCED_MONITORING.md)

---

**GetHome** - Making every journey safer, one step at a time. 🚶‍♀️🛡️
