package com.example.gethome.message.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "message_logs")
public class MessageLog {
    @Id
    private String id;
    
    private String notificationId;
    private String userId;
    private String contactId;
    private String contactEmail;
    private String contactPhone;
    private MessageType messageType;
    private String subject;
    private String content;
    private MessageStatus status;
    private LocalDateTime sentAt;
    private LocalDateTime deliveredAt;
    private String deliveryId; // External service delivery ID
    private String errorMessage;
    private int retryCount;
    private LocalDateTime nextRetryAt;
    
    public enum MessageType {
        EMAIL, SMS
    }
    
    public enum MessageStatus {
        PENDING, SENT, DELIVERED, FAILED, RETRY
    }
} 