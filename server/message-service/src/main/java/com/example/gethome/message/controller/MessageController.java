package com.example.gethome.message.controller;

import com.example.gethome.message.model.MessageLog;
import com.example.gethome.message.service.EmailService;
import com.example.gethome.message.service.MessageLogService;
import com.example.gethome.message.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final EmailService emailService;
    private final SmsService smsService;
    private final MessageLogService messageLogService;

    @PostMapping("/email/send")
    public ResponseEntity<String> sendEmail(
            @RequestParam String toEmail,
            @RequestParam String subject,
            @RequestParam String templateName,
            @RequestBody Map<String, Object> variables,
            Authentication authentication) {
        String userId = authentication.getName();
        log.info("Email send request from user: {} to: {}", userId, toEmail);
        
        String deliveryId = emailService.sendCustomEmail(toEmail, subject, templateName, variables);
        return ResponseEntity.ok(deliveryId);
    }

    @PostMapping("/sms/send")
    public ResponseEntity<String> sendSms(
            @RequestParam String toPhoneNumber,
            @RequestParam String message,
            Authentication authentication) {
        String userId = authentication.getName();
        log.info("SMS send request from user: {} to: {}", userId, toPhoneNumber);
        
        String deliveryId = smsService.sendCustomSms(toPhoneNumber, message);
        return ResponseEntity.ok(deliveryId);
    }

    @PostMapping("/sms/test")
    public ResponseEntity<String> testSms(
            @RequestParam String toPhoneNumber,
            Authentication authentication) {
        String userId = authentication.getName();
        log.info("SMS test request from user: {} to: {}", userId, toPhoneNumber);
        
        String deliveryId = smsService.sendTestSms(toPhoneNumber);
        return ResponseEntity.ok(deliveryId);
    }

    @PostMapping("/welcome/email")
    public ResponseEntity<String> sendWelcomeEmail(
            @RequestParam String toEmail,
            @RequestParam String userName,
            Authentication authentication) {
        String userId = authentication.getName();
        log.info("Welcome email request from user: {} to: {}", userId, toEmail);
        
        String deliveryId = emailService.sendWelcomeEmail(toEmail, userName);
        return ResponseEntity.ok(deliveryId);
    }

    @PostMapping("/welcome/sms")
    public ResponseEntity<String> sendWelcomeSms(
            @RequestParam String toPhoneNumber,
            @RequestParam String userName,
            Authentication authentication) {
        String userId = authentication.getName();
        log.info("Welcome SMS request from user: {} to: {}", userId, toPhoneNumber);
        
        String deliveryId = smsService.sendWelcomeSms(toPhoneNumber, userName);
        return ResponseEntity.ok(deliveryId);
    }

    @GetMapping("/logs")
    public ResponseEntity<List<MessageLog>> getMessageLogs(Authentication authentication) {
        String userId = authentication.getName();
        log.info("Getting message logs for user: {}", userId);
        
        List<MessageLog> logs = messageLogService.getUserMessageLogs(userId);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/logs/{logId}")
    public ResponseEntity<MessageLog> getMessageLog(
            @PathVariable String logId,
            Authentication authentication) {
        String userId = authentication.getName();
        log.info("Getting message log {} for user: {}", logId, userId);
        
        MessageLog log = messageLogService.getMessageLog(logId, userId);
        return ResponseEntity.ok(log);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Message service is healthy");
    }
} 