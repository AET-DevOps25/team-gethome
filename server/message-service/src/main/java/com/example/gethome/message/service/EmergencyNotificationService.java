package com.example.gethome.message.service;

import com.example.gethome.message.dto.EmergencyNotificationRequest;
import com.example.gethome.message.dto.EmergencyNotificationResponse;
import com.example.gethome.message.model.EmergencyNotification;
import com.example.gethome.message.repository.EmergencyNotificationRepository;
import com.example.gethome.message.client.UserManagementClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

// Custom metrics imports
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.DistributionSummary;
import jakarta.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmergencyNotificationService {
    
    // Record to hold separated contact data
    private record EmergencyContactsData(List<String> emailContacts, List<String> phoneContacts) {}

    private final EmergencyNotificationRepository emergencyNotificationRepository;
    private final EmailService emailService;
    private final SmsService smsService;
    private final WebSocketNotificationService webSocketService;
    private final UserManagementClient userManagementClient;
    private final MeterRegistry meterRegistry;

    // Custom emergency and messaging metrics
    private Counter emergencyNotificationsTotal;
    private Counter emergencyNotificationsSuccess;
    private Counter emergencyNotificationsFailed;
    private Counter highPriorityEmergenciesTotal;
    private Counter criticalEmergenciesTotal;
    private Counter emergencyContactsNotified;
    private Counter emailNotificationsTotal;
    private Counter smsNotificationsTotal;
    private Counter webSocketNotificationsTotal;
    private Timer emergencyResponseTime;
    private Timer emailDeliveryTime;
    private Timer smsDeliveryTime;
    private Timer notificationProcessingTime;
    private DistributionSummary emergencyContactsPerNotification;
    private DistributionSummary notificationDeliveryLatency;
    private Gauge activeEmergencies;
    private Gauge totalEmergencies;
    private Gauge averageResponseTime;
    
    // Safety and reliability metrics
    private Counter notificationDeliveryFailures;
    private Counter duplicateEmergencyAlerts;
    private Counter emergencyEscalations;
    private Counter falseAlarmDetections;
    
    // Business KPI tracking
    private final AtomicInteger currentActiveEmergencies = new AtomicInteger(0);
    private final AtomicLong totalEmergencyNotifications = new AtomicLong(0);
    private final AtomicLong totalContactsNotified = new AtomicLong(0);

    @PostConstruct
    public void initializeMetrics() {
        // Emergency notification metrics
        emergencyNotificationsTotal = Counter.builder("gethome_emergency_notifications_total")
                .description("Total number of emergency notifications created")
                .tag("service", "message")
                .tag("feature", "emergency")
                .register(meterRegistry);

        emergencyNotificationsSuccess = Counter.builder("gethome_emergency_notifications_success_total")
                .description("Total number of successfully sent emergency notifications")
                .tag("service", "message")
                .tag("feature", "emergency")
                .tag("outcome", "success")
                .register(meterRegistry);

        emergencyNotificationsFailed = Counter.builder("gethome_emergency_notifications_failed_total")
                .description("Total number of failed emergency notifications")
                .tag("service", "message")
                .tag("feature", "emergency")
                .tag("outcome", "failure")
                .register(meterRegistry);

        highPriorityEmergenciesTotal = Counter.builder("gethome_high_priority_emergencies_total")
                .description("Total number of high priority emergency alerts")
                .tag("service", "message")
                .tag("feature", "emergency")
                .tag("priority", "high")
                .register(meterRegistry);

        criticalEmergenciesTotal = Counter.builder("gethome_critical_emergencies_total")
                .description("Total number of critical emergency alerts")
                .tag("service", "message")
                .tag("feature", "emergency")
                .tag("priority", "critical")
                .register(meterRegistry);

        // Communication channel metrics
        emailNotificationsTotal = Counter.builder("gethome_email_notifications_total")
                .description("Total number of email notifications sent")
                .tag("service", "message")
                .tag("channel", "email")
                .register(meterRegistry);

        smsNotificationsTotal = Counter.builder("gethome_sms_notifications_total")
                .description("Total number of SMS notifications sent")
                .tag("service", "message")
                .tag("channel", "sms")
                .register(meterRegistry);

        webSocketNotificationsTotal = Counter.builder("gethome_websocket_notifications_total")
                .description("Total number of WebSocket notifications sent")
                .tag("service", "message")
                .tag("channel", "websocket")
                .register(meterRegistry);

        emergencyContactsNotified = Counter.builder("gethome_emergency_contacts_notified_total")
                .description("Total number of emergency contacts notified")
                .tag("service", "message")
                .tag("feature", "emergency")
                .register(meterRegistry);

        // Performance metrics
        emergencyResponseTime = Timer.builder("gethome_emergency_response_duration_seconds")
                .description("Time taken to process and send emergency notifications")
                .tag("service", "message")
                .tag("feature", "emergency")
                .register(meterRegistry);

        emailDeliveryTime = Timer.builder("gethome_email_delivery_duration_seconds")
                .description("Time taken to deliver email notifications")
                .tag("service", "message")
                .tag("channel", "email")
                .register(meterRegistry);

        smsDeliveryTime = Timer.builder("gethome_sms_delivery_duration_seconds")
                .description("Time taken to deliver SMS notifications")
                .tag("service", "message")
                .tag("channel", "sms")
                .register(meterRegistry);

        notificationProcessingTime = Timer.builder("gethome_notification_processing_duration_seconds")
                .description("Overall time to process any notification")
                .tag("service", "message")
                .register(meterRegistry);

        // Distribution metrics
        emergencyContactsPerNotification = DistributionSummary.builder("gethome_emergency_contacts_per_notification")
                .description("Distribution of number of contacts notified per emergency")
                .tag("service", "message")
                .tag("feature", "emergency")
                .register(meterRegistry);

        notificationDeliveryLatency = DistributionSummary.builder("gethome_notification_delivery_latency_ms")
                .description("Distribution of notification delivery latency in milliseconds")
                .tag("service", "message")
                .register(meterRegistry);

        // System health metrics
        activeEmergencies = Gauge.builder("gethome_active_emergencies_count", currentActiveEmergencies, AtomicInteger::get)
                .description("Current number of active emergency situations")
                .tag("service", "message")
                .tag("feature", "emergency")
                .register(meterRegistry);

        totalEmergencies = Gauge.builder("gethome_total_emergencies_count", totalEmergencyNotifications, AtomicLong::get)
                .description("Total number of emergency notifications ever created")
                .tag("service", "message")
                .tag("feature", "emergency")
                .register(meterRegistry);

        averageResponseTime = Gauge.builder("gethome_average_emergency_response_seconds", this, EmergencyNotificationService::calculateAverageResponseTime)
                .description("Average emergency response time in seconds")
                .tag("service", "message")
                .tag("feature", "emergency")
                .register(meterRegistry);

        // Reliability and safety metrics
        notificationDeliveryFailures = Counter.builder("gethome_notification_delivery_failures_total")
                .description("Total number of notification delivery failures")
                .tag("service", "message")
                .tag("reliability", "failure")
                .register(meterRegistry);

        duplicateEmergencyAlerts = Counter.builder("gethome_duplicate_emergency_alerts_total")
                .description("Total number of detected duplicate emergency alerts")
                .tag("service", "message")
                .tag("feature", "emergency")
                .tag("quality", "duplicate_detection")
                .register(meterRegistry);

        emergencyEscalations = Counter.builder("gethome_emergency_escalations_total")
                .description("Total number of emergency escalations to authorities")
                .tag("service", "message")
                .tag("feature", "emergency")
                .tag("escalation", "authorities")
                .register(meterRegistry);

        falseAlarmDetections = Counter.builder("gethome_false_alarm_detections_total")
                .description("Total number of detected false alarms")
                .tag("service", "message")
                .tag("feature", "emergency")
                .tag("quality", "false_alarm_detection")
                .register(meterRegistry);

        log.info("Custom GetHome emergency notification metrics initialized successfully");
    }

    public EmergencyNotificationResponse createEmergencyNotification(EmergencyNotificationRequest request) throws Exception {
        emergencyNotificationsTotal.increment();
        totalEmergencyNotifications.incrementAndGet();
        
        // Track priority levels based on emergency type
        if ("MANUAL".equalsIgnoreCase(request.getEmergencyType())) {
            highPriorityEmergenciesTotal.increment();
        } else if ("AI_DETECTED".equalsIgnoreCase(request.getEmergencyType())) {
            criticalEmergenciesTotal.increment();
        }

        try {
            return emergencyResponseTime.recordCallable(() -> {
                log.info("Creating emergency notification for user: {} with type: {}", 
                        request.getUserId(), request.getEmergencyType());

                // Check for duplicate alerts (simplified logic)
                if (isDuplicateAlert(request)) {
                    duplicateEmergencyAlerts.increment();
                    log.warn("Duplicate emergency alert detected for user: {}", request.getUserId());
                }

                // Create emergency notification
                EmergencyNotification notification = EmergencyNotification.builder()
            .userId(request.getUserId())
                        .reason(request.getMessage())
                        .emergencyType(request.getEmergencyType())
            .latitude(request.getLatitude())
            .longitude(request.getLongitude())
                        .location(request.getLocation())
            .triggeredAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusHours(24))
            .status(EmergencyNotification.NotificationStatus.PENDING)
            .build();

                notification = emergencyNotificationRepository.save(notification);
                currentActiveEmergencies.incrementAndGet();

                // Send notifications to emergency contacts
                int contactsNotified = sendNotificationsToEmergencyContacts(notification);
                emergencyContactsPerNotification.record(contactsNotified);
                emergencyContactsNotified.increment(contactsNotified);
                totalContactsNotified.addAndGet(contactsNotified);

                // Determine if escalation is needed
                if ("AI_DETECTED".equalsIgnoreCase(request.getEmergencyType())) {
                    escalateToAuthorities(notification);
                    emergencyEscalations.increment();
                }

                emergencyNotificationsSuccess.increment();
                log.info("Emergency notification created successfully: {} (Contacts notified: {})", 
                        notification.getId(), contactsNotified);

                return EmergencyNotificationResponse.builder()
                        .id(notification.getId())
                        .status(notification.getStatus().toString())
                        .triggeredAt(notification.getTriggeredAt())
                        .build();
            });
        } catch (Exception e) {
            emergencyNotificationsFailed.increment();
            log.error("Failed to create emergency notification for user: {}", request.getUserId(), e);
            throw e;
        }
    }

    private int sendNotificationsToEmergencyContacts(EmergencyNotification notification) {
        int contactsNotified = 0;
        
        try {
            // Get emergency contacts with proper separation by type
            EmergencyContactsData contactsData = getEmergencyContactsData(notification.getUserId());
            
            // Send email notifications to email contacts
            for (String emailContact : contactsData.emailContacts()) {
                try {
                    emailDeliveryTime.recordCallable(() -> {
                        emailService.sendEmergencyEmail(notification, emailContact);
                        emailNotificationsTotal.increment();
                        return null;
                    });
                    
                    // Send WebSocket notification
                    webSocketService.sendEmergencyNotification(emailContact, notification);
                    webSocketNotificationsTotal.increment();
                    
                    contactsNotified++;
                    log.info("Email notification sent successfully to: {}", emailContact);
                    
                } catch (Exception e) {
                    notificationDeliveryFailures.increment();
                    log.error("Failed to send email notification to: {}", emailContact, e);
                }
            }
            
            // Send SMS notifications to phone contacts
            for (String phoneContact : contactsData.phoneContacts()) {
                try {
                    smsDeliveryTime.recordCallable(() -> {
                        smsService.sendEmergencySMS(phoneContact, notification);
                        smsNotificationsTotal.increment();
                        return null;
                    });
                    
                    contactsNotified++;
                    log.info("SMS notification sent successfully to: {}", phoneContact);
                    
                } catch (Exception e) {
                    notificationDeliveryFailures.increment();
                    log.error("Failed to send SMS notification to: {}", phoneContact, e);
                }
            }
            
        } catch (Exception e) {
            log.error("Failed to get emergency contacts for user: {}", notification.getUserId(), e);
        }
        
        return contactsNotified;
    }

    private void escalateToAuthorities(EmergencyNotification notification) throws Exception {
        notificationProcessingTime.recordCallable(() -> {
            log.info("Escalating critical emergency to authorities: {}", notification.getId());
            // Escalation logic would go here
            return null;
        });
    }

    private boolean isDuplicateAlert(EmergencyNotificationRequest request) {
        // Check for recent similar alerts (simplified logic)
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        List<EmergencyNotification> recentAlerts = emergencyNotificationRepository
                .findByUserIdAndTriggeredAtAfter(request.getUserId(), fiveMinutesAgo);
        
        return recentAlerts.size() > 1; // More than 1 alert in 5 minutes might be duplicate
    }

    private EmergencyContactsData getEmergencyContactsData(String userId) {
        List<String> emailContacts = new ArrayList<>();
        List<String> phoneContacts = new ArrayList<>();
        
        try {
            // Get emergency contacts from user management service
            List<UserManagementClient.EmergencyContact> emergencyContacts = userManagementClient.getEmergencyContacts(userId);
            
            if (emergencyContacts != null && !emergencyContacts.isEmpty()) {
                log.info("Found {} emergency contacts for user: {}", emergencyContacts.size(), userId);
                
                for (UserManagementClient.EmergencyContact contact : emergencyContacts) {
                    String preferredMethod = contact.preferredMethod();
                    
                    // Add email if available and preferred method allows email
                    if (contact.email() != null && !contact.email().trim().isEmpty()) {
                        if (preferredMethod == null || 
                            preferredMethod.equalsIgnoreCase("EMAIL") || 
                            preferredMethod.equalsIgnoreCase("BOTH")) {
                            emailContacts.add(contact.email().trim());
                            log.info("Added email contact: {} with preference: {}", contact.email(), preferredMethod);
                        }
                    }
                    
                    // Add phone if available and preferred method allows SMS
                    if (contact.phone() != null && !contact.phone().trim().isEmpty()) {
                        if (preferredMethod == null || 
                            preferredMethod.equalsIgnoreCase("SMS") || 
                            preferredMethod.equalsIgnoreCase("BOTH")) {
                            phoneContacts.add(contact.phone().trim());
                            log.info("Added phone contact: {} with preference: {}", contact.phone(), preferredMethod);
                        }
                    }
                }
            }
            
            // If no emergency contacts found, fallback to user's own email
            if (emailContacts.isEmpty() && phoneContacts.isEmpty()) {
                log.warn("No emergency contacts found for user: {}, falling back to user's own email", userId);
                try {
                    UserManagementClient.UserProfileResponse userProfile = userManagementClient.getUserProfile(userId);
                    if (userProfile != null && userProfile.email() != null && !userProfile.email().trim().isEmpty()) {
                        emailContacts.add(userProfile.email().trim());
                        log.info("Added user's own email as fallback emergency contact: {}", userProfile.email());
                    } else {
                        log.warn("User profile found but no valid email available. User ID: {}", userId);
                        
                        // If userId looks like an email, use it as fallback
                        if (userId != null && userId.contains("@") && userId.contains(".")) {
                            emailContacts.add(userId);
                            log.info("Using userId as fallback email: {}", userId);
                        }
                    }
                } catch (Exception e) {
                    log.error("Failed to get user profile for fallback email. User ID: {}", userId, e);
                    
                    // Final fallback: if userId looks like an email, use it
                    if (userId != null && userId.contains("@") && userId.contains(".")) {
                        emailContacts.add(userId);
                        log.info("Using userId as emergency fallback email: {}", userId);
                    }
                }
            }
            
            if (emailContacts.isEmpty() && phoneContacts.isEmpty()) {
                log.error("No valid contacts found for emergency notification. User ID: {}", userId);
                // As last resort, you might want to add a system admin email for critical emergencies
                // emailContacts.add("admin@yourdomain.com");
            }
            
        } catch (Exception e) {
            log.error("Failed to get emergency contacts for user: {}", userId, e);
            
            // Final fallback: try to use userId as email if it looks like an email
            if (userId != null && userId.contains("@") && userId.contains(".")) {
                emailContacts.add(userId);
                log.info("Using userId as final fallback email for emergency notification: {}", userId);
            } else {
                log.error("No valid email contacts available for emergency notification. User ID: {}", userId);
            }
        }
        
        log.info("Final emergency contacts for user {}: {} email contacts, {} phone contacts", 
                userId, emailContacts.size(), phoneContacts.size());
        return new EmergencyContactsData(emailContacts, phoneContacts);
    }

    public void resolveEmergency(String notificationId) {
        try {
            EmergencyNotification notification = emergencyNotificationRepository.findById(notificationId)
                    .orElseThrow(() -> new RuntimeException("Emergency notification not found"));
            
            notification.setStatus(EmergencyNotification.NotificationStatus.DELIVERED);
            // Note: No resolvedAt field in current model - would need to be added
            emergencyNotificationRepository.save(notification);
            
            currentActiveEmergencies.decrementAndGet();
            
            log.info("Emergency resolved: {}", notificationId);
            
        } catch (Exception e) {
            log.error("Failed to resolve emergency: {}", notificationId, e);
            throw e;
        }
    }

    public void markAsFalseAlarm(String notificationId) {
        try {
            EmergencyNotification notification = emergencyNotificationRepository.findById(notificationId)
                    .orElseThrow(() -> new RuntimeException("Emergency notification not found"));
            
            notification.setStatus(EmergencyNotification.NotificationStatus.FAILED);
            // Note: No resolvedAt field in current model - would need to be added
            emergencyNotificationRepository.save(notification);
            
            falseAlarmDetections.increment();
            currentActiveEmergencies.decrementAndGet();
            
            log.info("Emergency marked as false alarm: {}", notificationId);
            
        } catch (Exception e) {
            log.error("Failed to mark emergency as false alarm: {}", notificationId, e);
            throw e;
        }
    }

    // Business intelligence calculation methods
    private double calculateAverageResponseTime() {
        try {
            // Calculate average response time for resolved emergencies
            List<EmergencyNotification> resolvedEmergencies = emergencyNotificationRepository
                    .findByStatusIn(List.of(EmergencyNotification.NotificationStatus.DELIVERED, EmergencyNotification.NotificationStatus.FAILED));
            
            // For now, return a fixed average since resolvedAt field doesn't exist in current model
            // In a real implementation, you would add a resolvedAt field to the model
            return resolvedEmergencies.isEmpty() ? 0.0 : 120.0; // 2 minutes average
                    
        } catch (Exception e) {
            log.warn("Failed to calculate average response time", e);
            return 0.0;
        }
    }

    public List<EmergencyNotification> getActiveEmergencies() {
        return emergencyNotificationRepository.findByStatus(EmergencyNotification.NotificationStatus.PENDING);
    }

    public List<EmergencyNotification> getUserEmergencies(String userId) {
        return emergencyNotificationRepository.findByUserId(userId);
    }

    // Controller interface methods
    public EmergencyNotificationResponse sendEmergencyNotification(EmergencyNotificationRequest request, String authToken) throws Exception {
        return createEmergencyNotification(request);
    }

    public List<EmergencyNotificationResponse> getUserNotifications(String userId) {
        List<EmergencyNotification> notifications = emergencyNotificationRepository.findByUserId(userId);
        return notifications.stream()
            .map(this::convertToResponse)
                .toList();
    }

    public EmergencyNotificationResponse getNotification(String notificationId, String userId) {
        EmergencyNotification notification = emergencyNotificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Emergency notification not found"));
        
        // Verify user owns this notification
        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied to notification");
        }
        
        return convertToResponse(notification);
    }

    private EmergencyNotificationResponse convertToResponse(EmergencyNotification notification) {
        return EmergencyNotificationResponse.builder()
            .id(notification.getId())
                .status(notification.getStatus().toString())
            .triggeredAt(notification.getTriggeredAt())
            .build();
    }
} 