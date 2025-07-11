package com.example.gethome.message.service;

import com.example.gethome.message.client.UserManagementClient;
import com.example.gethome.message.model.EmergencyNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

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
            
            // Create email content manually
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
        String triggeredAt = notification.getTriggeredAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String message = notification.getMetadata() != null ? (String) notification.getMetadata().get("message") : "Emergency situation detected";
        String googleMapsLink = notification.getMetadata() != null ? (String) notification.getMetadata().get("googleMapsLink") : "#";
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Emergency Alert</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 20px; line-height: 1.6; }
                    .header { background-color: #dc3545; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { padding: 30px; background-color: #f8f9fa; border: 1px solid #dee2e6; border-radius: 0 0 8px 8px; }
                    .alert { color: #721c24; background-color: #f8d7da; padding: 15px; border-radius: 5px; margin: 20px 0; border-left: 4px solid #dc3545; }
                    .details { margin: 20px 0; background-color: white; padding: 20px; border-radius: 5px; border: 1px solid #dee2e6; }
                    .location-btn { 
                        display: inline-block; 
                        background-color: #28a745; 
                        color: white; 
                        padding: 12px 20px; 
                        text-decoration: none; 
                        border-radius: 5px; 
                        margin: 10px 0;
                        font-weight: bold;
                    }
                    .footer { color: #6c757d; font-size: 12px; margin-top: 30px; padding-top: 20px; border-top: 1px solid #dee2e6; }
                    .urgent { color: #dc3545; font-weight: bold; font-size: 18px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>🚨 EMERGENCY ALERT 🚨</h1>
                    <p class="urgent">IMMEDIATE ATTENTION REQUIRED</p>
                </div>
                <div class="content">
                    <p>Dear <strong>%s</strong>,</p>
                    <div class="alert">
                        <strong>⚠️ An emergency situation has been detected for one of your emergency contacts.</strong>
                    </div>
                    
                    <div class="details">
                        <h3>🚨 Emergency Details:</h3>
                        <ul>
                            <li><strong>Type:</strong> %s</li>
                            <li><strong>Reason:</strong> %s</li>
                            <li><strong>Location:</strong> %s</li>
                            <li><strong>Coordinates:</strong> %.6f, %.6f</li>
                            <li><strong>Time:</strong> %s</li>
                        </ul>
                        
                        <h3>📍 Live Location:</h3>
                        <p><a href="%s" class="location-btn" target="_blank">🗺️ View Location on Google Maps</a></p>
                        <p style="font-size: 14px; color: #6c757d;">Click the button above to see the exact location where help is needed.</p>
                    </div>
                    
                    <div style="background-color: #fff3cd; padding: 15px; border-radius: 5px; border-left: 4px solid #ffc107; margin: 20px 0;">
                        <p><strong>📱 What to do next:</strong></p>
                        <ul>
                            <li>Try to contact the person immediately</li>
                            <li>If you cannot reach them, consider contacting emergency services</li>
                            <li>Use the location link above to find them</li>
                            <li>Stay calm and act quickly</li>
                        </ul>
                    </div>
                    
                    <div style="background-color: #e7f3ff; padding: 15px; border-radius: 5px; border-left: 4px solid #007bff; margin: 20px 0;">
                        <h4>📨 Full Message:</h4>
                        <p style="white-space: pre-line;">%s</p>
                    </div>
                </div>
                <div class="footer">
                    <p>This is an automated emergency alert from GetHome Safety System.</p>
                    <p>Generated at: %s | Emergency ID: %s</p>
                </div>
            </body>
            </html>
            """,
            contact.name(),
            notification.getEmergencyType(),
            notification.getReason(),
            notification.getLocation(),
            notification.getLatitude(),
            notification.getLongitude(),
            triggeredAt,
            googleMapsLink,
            message,
            triggeredAt,
            notification.getId() != null ? notification.getId() : "N/A"
        );
    }

    public String sendWelcomeEmail(String toEmail, String userName) {
        try {
            log.info("Sending welcome email to: {}", toEmail);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Welcome to GetHome - Your Safety Companion");
            
            // Create welcome email content manually
            String htmlContent = createWelcomeEmailContent(userName);
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

    private String createWelcomeEmailContent(String userName) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Welcome to GetHome</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 20px; }
                    .header { background-color: #007bff; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f8f9fa; border: 1px solid #dee2e6; }
                    .footer { color: #6c757d; font-size: 12px; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>Welcome to GetHome!</h1>
                </div>
                <div class="content">
                    <p>Dear %s,</p>
                    <p>Welcome to GetHome - your personal safety companion!</p>
                    <p>Our app helps ensure your safety by:</p>
                    <ul>
                        <li>Providing safe route recommendations</li>
                        <li>Emergency contact notifications</li>
                        <li>Real-time safety monitoring</li>
                        <li>Danger zone awareness</li>
                    </ul>
                    <p>Thank you for choosing GetHome to keep you safe!</p>
                </div>
                <div class="footer">
                    <p>Best regards,<br>The GetHome Team</p>
                </div>
            </body>
            </html>
            """, userName);
    }

    public String sendCustomEmail(String toEmail, String subject, String htmlContent) {
        try {
            log.info("Sending custom email to: {}", toEmail);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
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

    /**
     * Send emergency email to a contact (overloaded method for EmergencyNotificationService)
     */
    public String sendEmergencyEmail(EmergencyNotification notification, String contactEmail) {
        try {
            log.info("Sending emergency email to contact: {}", contactEmail);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(contactEmail);
            helper.setSubject(emergencySubject);
            
            String htmlContent = createEmergencyEmailContentForContact(notification, contactEmail);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            
            String deliveryId = UUID.randomUUID().toString();
            log.info("Emergency email sent successfully to {} with delivery ID: {}", contactEmail, deliveryId);
            
            return deliveryId;
            
        } catch (MessagingException e) {
            log.error("Failed to send emergency email to: {}", contactEmail, e);
            throw new RuntimeException("Email sending failed", e);
        }
    }

    private String createEmergencyEmailContentForContact(EmergencyNotification notification, String contactEmail) {
        String triggeredAt = notification.getTriggeredAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String googleMapsLink = String.format("https://www.google.com/maps?q=%.6f,%.6f", 
                notification.getLatitude(), notification.getLongitude());
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Emergency Alert</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 20px; line-height: 1.6; }
                    .header { background-color: #dc3545; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { padding: 30px; background-color: #f8f9fa; border: 1px solid #dee2e6; border-radius: 0 0 8px 8px; }
                    .alert { color: #721c24; background-color: #f8d7da; padding: 15px; border-radius: 5px; margin: 20px 0; border-left: 4px solid #dc3545; }
                    .details { margin: 20px 0; background-color: white; padding: 20px; border-radius: 5px; border: 1px solid #dee2e6; }
                    .location-btn { 
                        display: inline-block; 
                        background-color: #28a745; 
                        color: white; 
                        padding: 12px 20px; 
                        text-decoration: none; 
                        border-radius: 5px; 
                        margin: 10px 0;
                        font-weight: bold;
                    }
                    .footer { color: #6c757d; font-size: 12px; margin-top: 30px; padding-top: 20px; border-top: 1px solid #dee2e6; }
                    .urgent { color: #dc3545; font-weight: bold; font-size: 18px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>🚨 EMERGENCY ALERT 🚨</h1>
                    <p class="urgent">IMMEDIATE ATTENTION REQUIRED</p>
                </div>
                <div class="content">
                    <p>Dear Emergency Contact,</p>
                    <div class="alert">
                        <strong>⚠️ An emergency situation has been detected for one of your contacts.</strong>
                    </div>
                    
                    <div class="details">
                        <h3>🚨 Emergency Details:</h3>
                        <ul>
                            <li><strong>Type:</strong> %s</li>
                            <li><strong>Reason:</strong> %s</li>
                            <li><strong>Location:</strong> %s</li>
                            <li><strong>Coordinates:</strong> %.6f, %.6f</li>
                            <li><strong>Time:</strong> %s</li>
                        </ul>
                        
                        <h3>📍 Live Location:</h3>
                        <p><a href="%s" class="location-btn" target="_blank">🗺️ View Location on Google Maps</a></p>
                        <p style="font-size: 14px; color: #6c757d;">Click the button above to see the exact location where help is needed.</p>
                    </div>
                    
                    <div style="background-color: #fff3cd; padding: 15px; border-radius: 5px; border-left: 4px solid #ffc107; margin: 20px 0;">
                        <p><strong>📱 What to do next:</strong></p>
                        <ul>
                            <li>Try to contact the person immediately</li>
                            <li>If you cannot reach them, consider contacting emergency services</li>
                            <li>Use the location link above to find them</li>
                            <li>Stay calm and act quickly</li>
                        </ul>
                    </div>
                    
                    <div style="background-color: #e7f3ff; padding: 15px; border-radius: 5px; border-left: 4px solid #007bff; margin: 20px 0;">
                        <h4>📨 Full Message:</h4>
                        <p style="white-space: pre-line;">%s</p>
                    </div>
                </div>
                <div class="footer">
                    <p>This is an automated emergency alert from GetHome Safety System.</p>
                    <p>Generated at: %s | Emergency ID: %s</p>
                </div>
            </body>
            </html>
            """,
            notification.getEmergencyType(),
            notification.getReason(),
            notification.getLocation(),
            notification.getLatitude(),
            notification.getLongitude(),
            triggeredAt,
            googleMapsLink,
            notification.getReason(),
            triggeredAt,
            notification.getId() != null ? notification.getId() : "N/A"
        );
    }
} 