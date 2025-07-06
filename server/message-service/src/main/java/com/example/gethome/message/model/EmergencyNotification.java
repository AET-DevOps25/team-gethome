package com.example.gethome.message.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "emergency_notifications")
public class EmergencyNotification {
    @Id
    private String id;
    
    private String userId;
    private String emergencyType; // MANUAL, AUDIO, AI_DETECTED
    private String reason;
    private double latitude;
    private double longitude;
    private String location;
    private String audioSnippet;
    private LocalDateTime triggeredAt;
    private LocalDateTime expiresAt;
    
    private List<ContactNotification> contactNotifications;
    private NotificationStatus status;
    private Map<String, Object> metadata; // Additional context data
    
    public enum NotificationStatus {
        PENDING, SENT, DELIVERED, FAILED, EXPIRED
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContactNotification {
        private String contactId;
        private String contactName;
        private String contactEmail;
        private String contactPhone;
        private PreferredContactMethod preferredMethod;
        private NotificationStatus status;
        private LocalDateTime sentAt;
        private String deliveryId; // External service delivery ID
        private String errorMessage;
        
        public enum PreferredContactMethod {
            EMAIL, SMS, BOTH
        }
    }
} 