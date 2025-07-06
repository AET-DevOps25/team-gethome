package com.example.gethome.message.service;

import com.example.gethome.message.client.UserManagementClient;
import com.example.gethome.message.model.EmergencyNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final MessageTemplateService messageTemplateService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${message.emergency.subject}")
    private String emergencySubject;

    public String sendEmergencyEmail(EmergencyNotification notification, UserManagementClient.EmergencyContact contact) {
        try {
            log.info("Sending emergency email to: {}", contact.email());
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(contact.email());
            helper.setSubject(emergencySubject);
            
            // Create email content using template
            String htmlContent = createEmergencyEmailContent(notification, contact);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            
            String deliveryId = UUID.randomUUID().toString();
            log.info("Emergency email sent successfully to {} with delivery ID: {}", contact.email(), deliveryId);
            
            return deliveryId;
            
        } catch (MessagingException e) {
            log.error("Failed to send emergency email to: {}", contact.email(), e);
            throw new RuntimeException("Email sending failed", e);
        }
    }

    private String createEmergencyEmailContent(EmergencyNotification notification, UserManagementClient.EmergencyContact contact) {
        Context context = new Context();
        
        // Add template variables
        Map<String, Object> variables = new HashMap<>();
        variables.put("contactName", contact.name());
        variables.put("userName", notification.getUserId()); // Could be enhanced with actual user name
        variables.put("emergencyType", notification.getEmergencyType());
        variables.put("reason", notification.getReason());
        variables.put("latitude", notification.getLatitude());
        variables.put("longitude", notification.getLongitude());
        variables.put("location", notification.getLocation());
        variables.put("triggeredAt", notification.getTriggeredAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        variables.put("message", notification.getMetadata().get("message"));
        variables.put("audioSnippet", notification.getAudioSnippet());
        
        context.setVariables(variables);
        
        // Use emergency alert template
        return templateEngine.process("emergency-alert", context);
    }

    public String sendWelcomeEmail(String toEmail, String userName) {
        try {
            log.info("Sending welcome email to: {}", toEmail);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Welcome to GetHome - Your Safety Companion");
            
            // Create welcome email content
            Context context = new Context();
            context.setVariable("userName", userName);
            String htmlContent = templateEngine.process("welcome-message", context);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            
            String deliveryId = UUID.randomUUID().toString();
            log.info("Welcome email sent successfully to {} with delivery ID: {}", toEmail, deliveryId);
            
            return deliveryId;
            
        } catch (MessagingException e) {
            log.error("Failed to send welcome email to: {}", toEmail, e);
            throw new RuntimeException("Welcome email sending failed", e);
        }
    }

    public String sendCustomEmail(String toEmail, String subject, String templateName, Map<String, Object> variables) {
        try {
            log.info("Sending custom email to: {} using template: {}", toEmail, templateName);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            
            // Create email content using template
            Context context = new Context();
            context.setVariables(variables);
            String htmlContent = templateEngine.process(templateName, context);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            
            String deliveryId = UUID.randomUUID().toString();
            log.info("Custom email sent successfully to {} with delivery ID: {}", toEmail, deliveryId);
            
            return deliveryId;
            
        } catch (MessagingException e) {
            log.error("Failed to send custom email to: {}", toEmail, e);
            throw new RuntimeException("Custom email sending failed", e);
        }
    }
} 