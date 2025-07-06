package com.example.gethome.message.service;

import com.example.gethome.message.model.MessageTemplate;
import com.example.gethome.message.repository.MessageTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageTemplateService {

    private final MessageTemplateRepository messageTemplateRepository;

    public MessageTemplate createTemplate(MessageTemplate template) {
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        template.setActive(true);
        
        log.info("Creating message template: {}", template.getName());
        return messageTemplateRepository.save(template);
    }

    public MessageTemplate updateTemplate(String templateId, MessageTemplate updatedTemplate) {
        MessageTemplate existingTemplate = messageTemplateRepository.findById(templateId)
            .orElseThrow(() -> new RuntimeException("Template not found"));
        
        existingTemplate.setName(updatedTemplate.getName());
        existingTemplate.setDescription(updatedTemplate.getDescription());
        existingTemplate.setType(updatedTemplate.getType());
        existingTemplate.setSubject(updatedTemplate.getSubject());
        existingTemplate.setContent(updatedTemplate.getContent());
        existingTemplate.setHtmlContent(updatedTemplate.getHtmlContent());
        existingTemplate.setSmsContent(updatedTemplate.getSmsContent());
        existingTemplate.setVariables(updatedTemplate.getVariables());
        existingTemplate.setUpdatedAt(LocalDateTime.now());
        
        log.info("Updating message template: {}", existingTemplate.getName());
        return messageTemplateRepository.save(existingTemplate);
    }

    public MessageTemplate getTemplate(String templateId) {
        return messageTemplateRepository.findById(templateId)
            .orElseThrow(() -> new RuntimeException("Template not found"));
    }

    public Optional<MessageTemplate> getActiveTemplateByName(String name) {
        return messageTemplateRepository.findByNameAndIsActive(name, true);
    }

    public List<MessageTemplate> getActiveTemplates() {
        return messageTemplateRepository.findByIsActive(true);
    }

    public List<MessageTemplate> getTemplatesByType(MessageTemplate.TemplateType type) {
        return messageTemplateRepository.findByTypeAndIsActive(type, true);
    }

    public void deactivateTemplate(String templateId) {
        MessageTemplate template = getTemplate(templateId);
        template.setActive(false);
        template.setUpdatedAt(LocalDateTime.now());
        messageTemplateRepository.save(template);
        log.info("Deactivated message template: {}", template.getName());
    }

    public void deleteTemplate(String templateId) {
        MessageTemplate template = getTemplate(templateId);
        messageTemplateRepository.delete(template);
        log.info("Deleted message template: {}", template.getName());
    }

    public String processTemplate(String templateName, Map<String, Object> variables) {
        Optional<MessageTemplate> templateOpt = getActiveTemplateByName(templateName);
        if (templateOpt.isEmpty()) {
            throw new RuntimeException("Template not found: " + templateName);
        }
        
        MessageTemplate template = templateOpt.get();
        String content = template.getContent();
        
        // Simple variable replacement (in a real implementation, you might use a more sophisticated template engine)
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            content = content.replace(placeholder, value);
        }
        
        return content;
    }

    public String processHtmlTemplate(String templateName, Map<String, Object> variables) {
        Optional<MessageTemplate> templateOpt = getActiveTemplateByName(templateName);
        if (templateOpt.isEmpty()) {
            throw new RuntimeException("Template not found: " + templateName);
        }
        
        MessageTemplate template = templateOpt.get();
        String htmlContent = template.getHtmlContent();
        
        if (htmlContent == null) {
            htmlContent = template.getContent(); // Fallback to plain content
        }
        
        // Simple variable replacement
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            htmlContent = htmlContent.replace(placeholder, value);
        }
        
        return htmlContent;
    }

    public String processSmsTemplate(String templateName, Map<String, Object> variables) {
        Optional<MessageTemplate> templateOpt = getActiveTemplateByName(templateName);
        if (templateOpt.isEmpty()) {
            throw new RuntimeException("Template not found: " + templateName);
        }
        
        MessageTemplate template = templateOpt.get();
        String smsContent = template.getSmsContent();
        
        if (smsContent == null) {
            smsContent = template.getContent(); // Fallback to plain content
        }
        
        // Simple variable replacement
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            smsContent = smsContent.replace(placeholder, value);
        }
        
        return smsContent;
    }

    // Initialize default templates
    public void initializeDefaultTemplates() {
        if (getActiveTemplates().isEmpty()) {
            log.info("Initializing default message templates");
            
            // Emergency alert template
            MessageTemplate emergencyTemplate = MessageTemplate.builder()
                .name("emergency-alert")
                .description("Emergency alert email template")
                .type(MessageTemplate.TemplateType.EMAIL)
                .subject("EMERGENCY ALERT - GetHome User Needs Help")
                .htmlContent(createEmergencyEmailTemplate())
                .isActive(true)
                .build();
            createTemplate(emergencyTemplate);
            
            // Welcome message template
            MessageTemplate welcomeTemplate = MessageTemplate.builder()
                .name("welcome-message")
                .description("Welcome email template for new users")
                .type(MessageTemplate.TemplateType.EMAIL)
                .subject("Welcome to GetHome - Your Safety Companion")
                .htmlContent(createWelcomeEmailTemplate())
                .isActive(true)
                .build();
            createTemplate(welcomeTemplate);
        }
    }

    private String createEmergencyEmailTemplate() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Emergency Alert</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f4f4f4; }
                    .container { max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    .header { background-color: #dc3545; color: white; padding: 20px; text-align: center; border-radius: 5px; margin-bottom: 20px; }
                    .content { line-height: 1.6; }
                    .location { background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin: 15px 0; }
                    .footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; text-align: center; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🚨 EMERGENCY ALERT 🚨</h1>
                    </div>
                    <div class="content">
                        <p>Dear {{contactName}},</p>
                        <p><strong>{{userName}}</strong> has triggered an emergency alert and needs immediate assistance.</p>
                        
                        <div class="location">
                            <h3>Emergency Details:</h3>
                            <p><strong>Type:</strong> {{emergencyType}}</p>
                            <p><strong>Reason:</strong> {{reason}}</p>
                            <p><strong>Time:</strong> {{triggeredAt}}</p>
                            <p><strong>Location:</strong> {{latitude}}, {{longitude}}</p>
                            <p><strong>Address:</strong> {{location}}</p>
                        </div>
                        
                        <p><strong>Message:</strong> {{message}}</p>
                        
                        <p style="color: #dc3545; font-weight: bold;">Please respond immediately and take appropriate action!</p>
                    </div>
                    <div class="footer">
                        <p>This is an automated emergency notification from GetHome</p>
                    </div>
                </div>
            </body>
            </html>
            """;
    }

    private String createWelcomeEmailTemplate() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Welcome to GetHome</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f4f4f4; }
                    .container { max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    .header { background-color: #28a745; color: white; padding: 20px; text-align: center; border-radius: 5px; margin-bottom: 20px; }
                    .content { line-height: 1.6; }
                    .footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; text-align: center; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🏠 Welcome to GetHome</h1>
                    </div>
                    <div class="content">
                        <p>Dear {{userName}},</p>
                        <p>Welcome to GetHome - your personal safety companion!</p>
                        <p>We're here to keep you safe on your journeys, especially during late-night walks. Our features include:</p>
                        <ul>
                            <li>🛣️ Safe route planning with danger zone avoidance</li>
                            <li>🤖 AI companion for real-time support</li>
                            <li>🚨 Emergency alerts to your trusted contacts</li>
                            <li>📍 Real-time location tracking and sharing</li>
                        </ul>
                        <p>Stay safe and enjoy your walks with confidence!</p>
                    </div>
                    <div class="footer">
                        <p>Thank you for choosing GetHome</p>
                    </div>
                </div>
            </body>
            </html>
            """;
    }
} 