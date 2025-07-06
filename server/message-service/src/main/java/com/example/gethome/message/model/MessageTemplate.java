package com.example.gethome.message.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "message_templates")
public class MessageTemplate {
    @Id
    private String id;
    
    private String name;
    private String description;
    private TemplateType type;
    private String subject; // For email templates
    private String content;
    private String htmlContent; // For email templates
    private String smsContent; // For SMS templates
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Map<String, String> variables; // Template variables
    
    public enum TemplateType {
        EMAIL, SMS, BOTH
    }
} 