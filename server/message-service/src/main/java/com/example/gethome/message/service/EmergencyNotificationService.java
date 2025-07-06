package com.example.gethome.message.service;

import com.example.gethome.message.client.UserManagementClient;
import com.example.gethome.message.dto.EmergencyNotificationRequest;
import com.example.gethome.message.dto.EmergencyNotificationResponse;
import com.example.gethome.message.model.EmergencyNotification;
import com.example.gethome.message.model.MessageLog;
import com.example.gethome.message.repository.EmergencyNotificationRepository;
import com.example.gethome.message.repository.MessageLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmergencyNotificationService {

    private final EmergencyNotificationRepository emergencyNotificationRepository;
    private final MessageLogRepository messageLogRepository;
    private final UserManagementClient userManagementClient;
    private final EmailService emailService;
    private final SmsService smsService;
    private final MessageTemplateService messageTemplateService;

    public EmergencyNotificationResponse sendEmergencyNotification(EmergencyNotificationRequest request, String authToken) {
        log.warn("Processing emergency notification for user: {}", request.getUserId());
        
        try {
            // Get user's emergency contacts
            UserManagementClient.EmergencyContactsResponse contactsResponse = 
                userManagementClient.getEmergencyContacts(request.getUserId(), "Bearer " + authToken);
            
            if (contactsResponse.emergencyContacts().isEmpty()) {
                log.warn("No emergency contacts found for user: {}", request.getUserId());
                return EmergencyNotificationResponse.builder()
                    .id("no-contacts")
                    .userId(request.getUserId())
                    .status("NO_CONTACTS")
                    .message("No emergency contacts found")
                    .build();
            }
            
            // Create emergency notification record
            EmergencyNotification notification = createEmergencyNotification(request, contactsResponse);
            notification = emergencyNotificationRepository.save(notification);
            
            // Send notifications to each contact
            List<EmergencyNotificationResponse.ContactNotificationResult> contactResults = 
                sendNotificationsToContacts(notification, contactsResponse.emergencyContacts(), authToken);
            
            // Update notification status
            updateNotificationStatus(notification, contactResults);
            
            return EmergencyNotificationResponse.builder()
                .id(notification.getId())
                .userId(request.getUserId())
                .status(notification.getStatus().name())
                .triggeredAt(notification.getTriggeredAt())
                .expiresAt(notification.getExpiresAt())
                .contactResults(contactResults)
                .message("Emergency notification sent successfully")
                .build();
                
        } catch (Exception e) {
            log.error("Failed to send emergency notification for user: {}", request.getUserId(), e);
            throw new RuntimeException("Emergency notification failed", e);
        }
    }

    private EmergencyNotification createEmergencyNotification(EmergencyNotificationRequest request, 
                                                             UserManagementClient.EmergencyContactsResponse contactsResponse) {
        List<EmergencyNotification.ContactNotification> contactNotifications = contactsResponse.emergencyContacts().stream()
            .map(contact -> EmergencyNotification.ContactNotification.builder()
                .contactId(contact.id())
                .contactName(contact.name())
                .contactEmail(contact.email())
                .contactPhone(contact.phone())
                .preferredMethod(parsePreferredMethod(contact.preferredMethod()))
                .status(EmergencyNotification.NotificationStatus.PENDING)
                .build())
            .collect(Collectors.toList());

        return EmergencyNotification.builder()
            .userId(request.getUserId())
            .emergencyType(request.getEmergencyType() != null ? request.getEmergencyType() : "MANUAL")
            .reason(request.getReason())
            .latitude(request.getLatitude())
            .longitude(request.getLongitude())
            .location(request.getLocation())
            .audioSnippet(request.getAudioSnippet())
            .triggeredAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusHours(24))
            .contactNotifications(contactNotifications)
            .status(EmergencyNotification.NotificationStatus.PENDING)
            .metadata(Map.of("message", request.getMessage()))
            .build();
    }

    private List<EmergencyNotificationResponse.ContactNotificationResult> sendNotificationsToContacts(
            EmergencyNotification notification, 
            List<UserManagementClient.EmergencyContact> contacts,
            String authToken) {
        
        List<EmergencyNotificationResponse.ContactNotificationResult> results = new ArrayList<>();
        
        for (UserManagementClient.EmergencyContact contact : contacts) {
            EmergencyNotificationResponse.ContactNotificationResult result = 
                sendNotificationToContact(notification, contact);
            results.add(result);
        }
        
        return results;
    }

    private EmergencyNotificationResponse.ContactNotificationResult sendNotificationToContact(
            EmergencyNotification notification, 
            UserManagementClient.EmergencyContact contact) {
        
        try {
            EmergencyNotificationResponse.ContactNotificationResult.ContactNotificationResultBuilder resultBuilder = 
                EmergencyNotificationResponse.ContactNotificationResult.builder()
                    .contactId(contact.id())
                    .contactName(contact.name())
                    .contactEmail(contact.email())
                    .contactPhone(contact.phone())
                    .preferredMethod(contact.preferredMethod())
                    .sentAt(LocalDateTime.now());
            
            // Determine contact method and send notification
            String preferredMethod = contact.preferredMethod().toUpperCase();
            String deliveryId = null;
            String errorMessage = null;
            
            if ("EMAIL".equals(preferredMethod) || "BOTH".equals(preferredMethod)) {
                try {
                    deliveryId = emailService.sendEmergencyEmail(notification, contact);
                    resultBuilder.status("SENT");
                } catch (Exception e) {
                    errorMessage = "Email failed: " + e.getMessage();
                    resultBuilder.status("FAILED");
                }
            }
            
            if ("SMS".equals(preferredMethod) || "BOTH".equals(preferredMethod)) {
                try {
                    String smsDeliveryId = smsService.sendEmergencySms(notification, contact);
                    if (deliveryId == null) {
                        deliveryId = smsDeliveryId;
                    }
                    if (errorMessage == null) {
                        resultBuilder.status("SENT");
                    }
                } catch (Exception e) {
                    if (errorMessage == null) {
                        errorMessage = "SMS failed: " + e.getMessage();
                        resultBuilder.status("FAILED");
                    } else {
                        errorMessage += "; SMS failed: " + e.getMessage();
                    }
                }
            }
            
            resultBuilder.deliveryId(deliveryId);
            resultBuilder.errorMessage(errorMessage);
            
            // Log the message
            logMessage(notification, contact, preferredMethod, deliveryId, errorMessage);
            
            return resultBuilder.build();
            
        } catch (Exception e) {
            log.error("Failed to send notification to contact: {}", contact.id(), e);
            return EmergencyNotificationResponse.ContactNotificationResult.builder()
                .contactId(contact.id())
                .contactName(contact.name())
                .contactEmail(contact.email())
                .contactPhone(contact.phone())
                .preferredMethod(contact.preferredMethod())
                .status("FAILED")
                .sentAt(LocalDateTime.now())
                .errorMessage("Notification failed: " + e.getMessage())
                .build();
        }
    }

    private void updateNotificationStatus(EmergencyNotification notification, 
                                        List<EmergencyNotificationResponse.ContactNotificationResult> results) {
        boolean allFailed = results.stream().allMatch(r -> "FAILED".equals(r.getStatus()));
        boolean allSent = results.stream().allMatch(r -> "SENT".equals(r.getStatus()));
        
        if (allFailed) {
            notification.setStatus(EmergencyNotification.NotificationStatus.FAILED);
        } else if (allSent) {
            notification.setStatus(EmergencyNotification.NotificationStatus.SENT);
        } else {
            notification.setStatus(EmergencyNotification.NotificationStatus.SENT); // Partial success
        }
        
        emergencyNotificationRepository.save(notification);
    }

    private void logMessage(EmergencyNotification notification, 
                           UserManagementClient.EmergencyContact contact,
                           String messageType, 
                           String deliveryId, 
                           String errorMessage) {
        
        MessageLog messageLog = MessageLog.builder()
            .notificationId(notification.getId())
            .userId(notification.getUserId())
            .contactId(contact.id())
            .contactEmail(contact.email())
            .contactPhone(contact.phone())
            .messageType("EMAIL".equals(messageType) ? MessageLog.MessageType.EMAIL : MessageLog.MessageType.SMS)
            .subject("EMERGENCY ALERT - GetHome User Needs Help")
            .content(notification.getMetadata().get("message").toString())
            .status(errorMessage != null ? MessageLog.MessageStatus.FAILED : MessageLog.MessageStatus.SENT)
            .sentAt(LocalDateTime.now())
            .deliveryId(deliveryId)
            .errorMessage(errorMessage)
            .retryCount(0)
            .build();
        
        messageLogRepository.save(messageLog);
    }

    private EmergencyNotification.ContactNotification.PreferredContactMethod parsePreferredMethod(String method) {
        return switch (method.toUpperCase()) {
            case "EMAIL" -> EmergencyNotification.ContactNotification.PreferredContactMethod.EMAIL;
            case "SMS" -> EmergencyNotification.ContactNotification.PreferredContactMethod.SMS;
            case "BOTH" -> EmergencyNotification.ContactNotification.PreferredContactMethod.BOTH;
            default -> EmergencyNotification.ContactNotification.PreferredContactMethod.EMAIL;
        };
    }

    public List<EmergencyNotificationResponse> getUserNotifications(String userId) {
        List<EmergencyNotification> notifications = emergencyNotificationRepository.findByUserId(userId);
        return notifications.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    public EmergencyNotificationResponse getNotification(String notificationId, String userId) {
        EmergencyNotification notification = emergencyNotificationRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        
        return convertToResponse(notification);
    }

    private EmergencyNotificationResponse convertToResponse(EmergencyNotification notification) {
        List<EmergencyNotificationResponse.ContactNotificationResult> contactResults = 
            notification.getContactNotifications().stream()
                .map(contact -> EmergencyNotificationResponse.ContactNotificationResult.builder()
                    .contactId(contact.getContactId())
                    .contactName(contact.getContactName())
                    .contactEmail(contact.getContactEmail())
                    .contactPhone(contact.getContactPhone())
                    .preferredMethod(contact.getPreferredMethod().name())
                    .status(contact.getStatus().name())
                    .sentAt(contact.getSentAt())
                    .deliveryId(contact.getDeliveryId())
                    .errorMessage(contact.getErrorMessage())
                    .build())
                .collect(Collectors.toList());

        return EmergencyNotificationResponse.builder()
            .id(notification.getId())
            .userId(notification.getUserId())
            .status(notification.getStatus().name())
            .triggeredAt(notification.getTriggeredAt())
            .expiresAt(notification.getExpiresAt())
            .contactResults(contactResults)
            .message(notification.getMetadata().get("message").toString())
            .build();
    }
} 