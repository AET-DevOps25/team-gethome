# GetHome Routing Service

The Routing Service is a core component of the GetHome application that provides safe route planning with danger zone avoidance for solo pedestrians, particularly in late-night settings.

## Features

### 🛣️ Safe Route Planning
- Calculate optimal walking routes using OpenRouteService API
- Avoid reported danger zones based on crowd-sourced safety data
- Provide turn-by-turn navigation instructions
- Calculate safety scores for routes

### 🚨 Danger Zone Management
- Allow users to report unsafe locations (parks, alleys, etc.)
- Crowd-sourced danger zone database with expiration
- Categorize danger levels (LOW, MEDIUM, HIGH, CRITICAL)
- Tag-based organization (alley, poor_lighting, etc.)

### 🆘 Emergency Handling
- Process emergency triggers from users
- Integrate with message service for emergency notifications
- Send location and context to emergency contacts
- Support both manual and audio-based emergency detection

## Architecture

### Services
- **RoutingService**: Main route planning logic with danger zone avoidance
- **DangerZoneService**: Management of danger zone reports and queries
- **SafetyAnalysisService**: Calculate safety scores based on nearby danger zones
- **EmergencyService**: Handle emergency situations and notifications

### External Integrations
- **OpenRouteService**: Free-tier routing API for walking directions
- **UserManagementService**: Get user profiles and emergency contacts
- **MessageService**: Send emergency notifications to contacts

### Data Models
- **Route**: Complete route information with segments and safety metrics
- **DangerZone**: Geospatial danger zone data with metadata
- **RouteRequest/Response**: API DTOs for route planning

## API Endpoints

### Route Planning
```
POST /api/routes/plan
GET /api/routes
GET /api/routes/{routeId}
POST /api/routes/{routeId}/complete
```

### Danger Zones
```
POST /api/danger-zones/report
GET /api/danger-zones/nearby
GET /api/danger-zones/level/{level}
GET /api/danger-zones/tag/{tag}
GET /api/danger-zones/my-reports
GET /api/danger-zones/{zoneId}
PUT /api/danger-zones/{zoneId}
DELETE /api/danger-zones/{zoneId}
POST /api/danger-zones/cleanup
```

### Emergency
```
POST /api/emergency/trigger
POST /api/emergency/audio
POST /api/emergency/manual
```

## Configuration

### Environment Variables
```properties
# MongoDB
spring.data.mongodb.host=mongo
spring.data.mongodb.port=27017
spring.data.mongodb.database=gethome_routing

# JWT
jwt.secret=your-secret-key
jwt.expiration=86400000

# External Services
service.auth.url=http://auth-service:8081
service.usermanagement.url=http://usermanagement-service:8082
service.message.url=http://message-service:8083
service.ai.url=http://ai-service:8085

# OpenRouteService API
routing.api.url=https://api.openrouteservice.org/v2
routing.api.key=your-openrouteservice-api-key
```

## Setup

### Prerequisites
- Java 17+
- MongoDB
- OpenRouteService API key (free tier available)

### Running Locally
```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun
```

### Docker
```bash
# Build image
docker build -t gethome-routing-service .

# Run container
docker run -p 8084:8084 gethome-routing-service
```

## Safety Algorithm

The service uses a sophisticated safety scoring algorithm:

1. **Danger Zone Detection**: Find all active danger zones within 2km of route
2. **Distance Calculation**: Calculate minimum distance from route segments to danger zones
3. **Risk Assessment**: 
   - Base risk based on danger level (LOW=0.1, MEDIUM=0.3, HIGH=0.6, CRITICAL=0.9)
   - Distance factor (closer = higher risk)
   - Report count factor (more reports = higher risk)
4. **Safety Score**: Convert total risk to safety score (0.0 to 1.0)

## Testing

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests RoutingServiceTest

# Run with coverage
./gradlew test jacocoTestReport
```

## Monitoring

The service includes:
- Health check endpoint: `/actuator/health`
- Prometheus metrics: `/actuator/prometheus`
- Application metrics and logging

## Contributing

1. Follow the existing code structure and patterns
2. Add comprehensive tests for new features
3. Update documentation for API changes
4. Ensure all tests pass before submitting

## License

This project is part of the GetHome application and follows the same licensing terms. 