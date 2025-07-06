package com.example.routing_service.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class EmergencyNotificationResponse {
    private String id;
    private String userId;
    private String status;
    private String message;
    private LocalDateTime triggeredAt;
    private LocalDateTime expiresAt;
    private List<ContactNotificationResult> contactResults;
    
    @Data
    @Builder
    public static class ContactNotificationResult {
        private String contactId;
        private String contactName;
        private String contactEmail;
        private String contactPhone;
        private String preferredMethod;
        private String status;
        private LocalDateTime sentAt;
        private String deliveryId;
        private String errorMessage;
    }
} 