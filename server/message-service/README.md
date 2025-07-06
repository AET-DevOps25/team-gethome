# GetHome Message Service

The Message Service is a critical component of the GetHome application that handles emergency notifications and communication with emergency contacts. It provides reliable email and SMS delivery for urgent situations.

## Features

### 🚨 Emergency Notifications
- Process emergency triggers from users and other services
- Send immediate notifications to emergency contacts
- Support both email and SMS delivery methods
- Track delivery status and retry failed messages
- Provide detailed delivery logs and analytics

### 📧 Email Services
- HTML email templates with responsive design
- Emergency alert emails with location and context
- Welcome emails for new users
- Custom email templates with variable substitution
- SMTP integration (Gmail, SendGrid, etc.)

### 📱 SMS Services
- Twilio integration for reliable SMS delivery
- Emergency SMS with location and context
- Welcome SMS for new users
- Custom SMS messages
- Delivery status tracking

### 📊 Message Management
- Comprehensive message logging and tracking
- Delivery status monitoring (PENDING, SENT, DELIVERED, FAILED)
- Automatic retry mechanism for failed messages
- Message template management
- Cleanup of old message logs

## Architecture

### Services
- **EmergencyNotificationService**: Main emergency notification processing
- **EmailService**: Email delivery using Spring Mail and Thymeleaf templates
- **SmsService**: SMS delivery using Twilio API
- **MessageTemplateService**: Template management and variable substitution
- **MessageLogService**: Message delivery tracking and analytics

### External Integrations
- **UserManagementService**: Get user profiles and emergency contacts
- **Twilio**: SMS delivery service
- **SMTP Providers**: Email delivery (Gmail, SendGrid, etc.)

### Data Models
- **EmergencyNotification**: Complete emergency notification records
- **MessageLog**: Detailed message delivery logs
- **MessageTemplate**: Email and SMS templates with variables

## API Endpoints

### Emergency Notifications
```
POST /api/emergency/notify
GET /api/emergency/notifications
GET /api/emergency/notifications/{notificationId}
```

### General Messaging
```
POST /api/messages/email/send
POST /api/messages/sms/send
POST /api/messages/sms/test
POST /api/messages/welcome/email
POST /api/messages/welcome/sms
GET /api/messages/logs
GET /api/messages/logs/{logId}
```

## Configuration

### Environment Variables
```properties
# MongoDB
spring.data.mongodb.host=mongo
spring.data.mongodb.port=27017
spring.data.mongodb.database=gethome_messages

# JWT
jwt.secret=your-secret-key
jwt.expiration=86400000

# External Services
service.auth.url=http://auth-service:8081
service.usermanagement.url=http://usermanagement-service:8082
service.routing.url=http://routing-service:8084
service.ai.url=http://ai-service:8085

# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${EMAIL_USERNAME:your-email@gmail.com}
spring.mail.password=${EMAIL_PASSWORD:your-app-password}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Twilio Configuration
twilio.account.sid=${TWILIO_ACCOUNT_SID:your-twilio-account-sid}
twilio.auth.token=${TWILIO_AUTH_TOKEN:your-twilio-auth-token}
twilio.phone.number=${TWILIO_PHONE_NUMBER:+1234567890}

# Message Templates
message.emergency.subject=EMERGENCY ALERT - GetHome User Needs Help
message.emergency.template=emergency-alert
message.welcome.subject=Welcome to GetHome - Your Safety Companion
message.welcome.template=welcome-message
```

## Setup

### Prerequisites
- Java 17+
- MongoDB
- Twilio account (for SMS)
- SMTP provider (for email)

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
docker build -t gethome-message-service .

# Run container
docker run -p 8083:8083 gethome-message-service
```

## Emergency Notification Flow

1. **Emergency Trigger**: User or AI service triggers emergency
2. **Contact Retrieval**: Get user's emergency contacts from user management service
3. **Notification Creation**: Create emergency notification record
4. **Multi-Channel Delivery**: Send notifications via email and/or SMS based on contact preferences
5. **Status Tracking**: Track delivery status and retry failed messages
6. **Logging**: Log all message attempts for audit and analytics

## Message Templates

### Emergency Alert Email Template
- Professional HTML design with emergency styling
- Includes user location, emergency type, and context
- Clear call-to-action for emergency contacts
- Responsive design for mobile devices

### Emergency SMS Template
- Concise emergency information
- Location coordinates and address
- Emergency type and reason
- Clear urgency indicators

## Retry Mechanism

The service includes a robust retry mechanism for failed messages:

- **Automatic Retries**: Failed messages are automatically retried
- **Exponential Backoff**: Retry intervals increase with each attempt
- **Max Retries**: Maximum of 3 retry attempts per message
- **Status Tracking**: Track retry attempts and next retry time
- **Scheduled Processing**: Retry failed messages every 5 minutes

## Monitoring

The service includes comprehensive monitoring:

- **Health Check**: `/actuator/health`
- **Prometheus Metrics**: `/actuator/prometheus`
- **Message Delivery Metrics**: Track success/failure rates
- **Performance Metrics**: Response times and throughput
- **Error Tracking**: Detailed error logs and alerts

## Testing

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests EmergencyNotificationServiceTest

# Run with coverage
./gradlew test jacocoTestReport
```

## Scheduled Tasks

The service runs several scheduled tasks:

- **Daily Cleanup**: Remove message logs older than 30 days (2 AM)
- **Failed Message Retry**: Retry failed messages every 5 minutes
- **Template Initialization**: Initialize default templates daily (1 AM)

## Contributing

1. Follow the existing code structure and patterns
2. Add comprehensive tests for new features
3. Update documentation for API changes
4. Ensure all tests pass before submitting
5. Test email and SMS delivery in development environment

## License

This project is part of the GetHome application and follows the same licensing terms. 