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
        StringBuilder content = new StringBuilder();
        content.append("🚨 EMERGENCY ALERT 🚨\n\n");
        content.append("User: ").append(notification.getUserId()).append("\n");
        content.append("Type: ").append(notification.getEmergencyType()).append("\n");
        
        if (notification.getReason() != null) {
            content.append("Reason: ").append(notification.getReason()).append("\n");
        }
        
        content.append("Location: ").append(notification.getLatitude()).append(", ").append(notification.getLongitude()).append("\n");
        
        if (notification.getLocation() != null) {
            content.append("Address: ").append(notification.getLocation()).append("\n");
        }
        
        content.append("Time: ").append(notification.getTriggeredAt().format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))).append("\n\n");
        
        if (notification.getMetadata().get("message") != null) {
            content.append("Message: ").append(notification.getMetadata().get("message")).append("\n\n");
        }
        
        content.append("Please respond immediately!");
        
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
} 