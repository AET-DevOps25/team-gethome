package com.example.gethome.message.service;

import com.example.gethome.message.client.UserManagementClient;
import com.example.gethome.message.model.EmergencyNotification;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String fromPhoneNumber;

    public String sendEmergencySms(EmergencyNotification notification, UserManagementClient.EmergencyContact contact) {
        try {
            log.info("Sending emergency SMS to: {}", contact.phone());
            
            // Initialize Twilio
            Twilio.init(accountSid, authToken);
            
            // Create SMS content
            String smsContent = createEmergencySmsContent(notification, contact);
            
            // Send SMS
            Message message = Message.creator(
                new PhoneNumber(contact.phone()),
                new PhoneNumber(fromPhoneNumber),
                smsContent
            ).create();
            
            log.info("Emergency SMS sent successfully to {} with SID: {}", contact.phone(), message.getSid());
            
            return message.getSid();
            
        } catch (Exception e) {
            log.error("Failed to send emergency SMS to: {}", contact.phone(), e);
            throw new RuntimeException("SMS sending failed", e);
        }
    }

    private String createEmergencySmsContent(EmergencyNotification notification, UserManagementClient.EmergencyContact contact) {
        String googleMapsLink = notification.getMetadata() != null ? 
            (String) notification.getMetadata().get("googleMapsLink") : 
            String.format("https://www.google.com/maps?q=%.6f,%.6f", notification.getLatitude(), notification.getLongitude());
            
        StringBuilder content = new StringBuilder();
        content.append("🚨 EMERGENCY ALERT 🚨\n\n");
        content.append("Hi ").append(contact.name()).append(",\n\n");
        content.append("A GetHome user needs immediate help!\n\n");
        content.append("📍 Location: ").append(notification.getLocation()).append("\n");
        content.append("🗺️ Live Map: ").append(googleMapsLink).append("\n");
        content.append("⏰ Time: ").append(notification.getTriggeredAt().format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"))).append("\n");
        content.append("📝 Reason: ").append(notification.getReason()).append("\n\n");
        content.append("⚠️ Please respond immediately or call emergency services if needed!");
        
        return content.toString();
    }

    public String sendCustomSms(String toPhoneNumber, String message) {
        try {
            log.info("Sending custom SMS to: {}", toPhoneNumber);
            
            // Initialize Twilio
            Twilio.init(accountSid, authToken);
            
            // Send SMS
            Message twilioMessage = Message.creator(
                new PhoneNumber(toPhoneNumber),
                new PhoneNumber(fromPhoneNumber),
                message
            ).create();
            
            log.info("Custom SMS sent successfully to {} with SID: {}", toPhoneNumber, twilioMessage.getSid());
            
            return twilioMessage.getSid();
            
        } catch (Exception e) {
            log.error("Failed to send custom SMS to: {}", toPhoneNumber, e);
            throw new RuntimeException("Custom SMS sending failed", e);
        }
    }

    public String sendWelcomeSms(String toPhoneNumber, String userName) {
        String message = String.format(
            "Welcome to GetHome, %s! 🏠\n\n" +
            "Your safety companion is now active. " +
            "We'll keep you safe on your journeys.\n\n" +
            "Stay safe!",
            userName
        );
        
        return sendCustomSms(toPhoneNumber, message);
    }

    public String sendTestSms(String toPhoneNumber) {
        String message = "GetHome SMS test message. If you receive this, SMS notifications are working correctly!";
        return sendCustomSms(toPhoneNumber, message);
    }

    /**
     * Send emergency SMS to a contact (overloaded method for EmergencyNotificationService)
     */
    public String sendEmergencySMS(String contact, EmergencyNotification notification) {
        try {
            log.info("Sending emergency SMS to contact: {}", contact);
            
            // Initialize Twilio
            Twilio.init(accountSid, authToken);
            
            // Create SMS content for contact
            String smsContent = createEmergencySmsContentForContact(contact, notification);
            
            // Send SMS
            Message message = Message.creator(
                new PhoneNumber(contact),
                new PhoneNumber(fromPhoneNumber),
                smsContent
            ).create();
            
            log.info("Emergency SMS sent successfully to {} with SID: {}", contact, message.getSid());
            
            return message.getSid();
            
        } catch (Exception e) {
            log.error("Failed to send emergency SMS to: {}", contact, e);
            throw new RuntimeException("SMS sending failed", e);
        }
    }

    private String createEmergencySmsContentForContact(String contact, EmergencyNotification notification) {
        String googleMapsLink = String.format("https://www.google.com/maps?q=%.6f,%.6f", 
                notification.getLatitude(), notification.getLongitude());
            
        StringBuilder content = new StringBuilder();
        content.append("🚨 EMERGENCY ALERT 🚨\n\n");
        content.append("Hi,\n\n");
        content.append("A GetHome user needs immediate help!\n\n");
        content.append("📍 Location: ").append(notification.getLocation()).append("\n");
        content.append("🗺️ Live Map: ").append(googleMapsLink).append("\n");
        content.append("⏰ Time: ").append(notification.getTriggeredAt()).append("\n");
        content.append("📝 Reason: ").append(notification.getReason()).append("\n\n");
        content.append("⚠️ Please respond immediately or call emergency services if needed!");
        
        return content.toString();
    }
} 