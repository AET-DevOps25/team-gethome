package com.example.gethome.message.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyNotificationResponse {
    private String id;
    private String userId;
    private String status;
    private LocalDateTime triggeredAt;
    private LocalDateTime expiresAt;
    private List<ContactNotificationResult> contactResults;
    private String message;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
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