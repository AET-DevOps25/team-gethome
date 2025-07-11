package com.example.gethome.message.controller;

import com.example.gethome.message.dto.EmergencyNotificationRequest;
import com.example.gethome.message.dto.EmergencyNotificationResponse;
import com.example.gethome.message.service.EmergencyNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/emergency")
@RequiredArgsConstructor
@Slf4j
public class EmergencyController {

    private final EmergencyNotificationService emergencyNotificationService;

    @PostMapping("/notify")
    public ResponseEntity<EmergencyNotificationResponse> sendEmergencyNotification(
            @Valid @RequestBody EmergencyNotificationRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        log.warn("Emergency notification request from user: {}", userId);
        
        try {
            EmergencyNotificationResponse response = emergencyNotificationService.sendEmergencyNotification(request, "Bearer " + getAuthToken());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to send emergency notification for user: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<EmergencyNotificationResponse>> getUserNotifications(Authentication authentication) {
        String userId = authentication.getName();
        log.info("Getting emergency notifications for user: {}", userId);
        
        List<EmergencyNotificationResponse> notifications = emergencyNotificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/notifications/{notificationId}")
    public ResponseEntity<EmergencyNotificationResponse> getNotification(
            @PathVariable String notificationId,
            Authentication authentication) {
        String userId = authentication.getName();
        log.info("Getting emergency notification {} for user: {}", notificationId, userId);
        
        EmergencyNotificationResponse notification = emergencyNotificationService.getNotification(notificationId, userId);
        return ResponseEntity.ok(notification);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Emergency notification service is healthy");
    }

    private String getAuthToken() {
        // In a real implementation, this would get the service-to-service auth token
        // For now, return a placeholder
        return "service-token";
    }
} 