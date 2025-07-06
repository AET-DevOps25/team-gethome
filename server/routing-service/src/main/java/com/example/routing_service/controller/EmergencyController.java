package com.example.routing_service.controller;

import com.example.routing_service.dto.EmergencyNotificationRequest;
import com.example.routing_service.dto.EmergencyNotificationResponse;
import com.example.routing_service.service.EmergencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/emergency")
@RequiredArgsConstructor
@Slf4j
public class EmergencyController {

    private final EmergencyService emergencyService;

    @PostMapping("/trigger")
    public ResponseEntity<EmergencyNotificationResponse> triggerEmergency(
            @RequestBody EmergencyNotificationRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        log.warn("Emergency trigger request from user: {} at location: {},{}", userId, request.getLatitude(), request.getLongitude());
        
        EmergencyNotificationResponse response = emergencyService.triggerEmergency(
            userId,
            request.getReason(),
            request.getLatitude(),
            request.getLongitude(),
            request.getLocation(),
            request.getAudioSnippet(),
            getAuthToken()
        );
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/trigger-audio")
    public ResponseEntity<EmergencyNotificationResponse> triggerAudioEmergency(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam String location,
            @RequestParam String audioSnippet,
            Authentication authentication) {
        String userId = authentication.getName();
        log.warn("Audio emergency trigger request from user: {} at location: {},{}", userId, latitude, longitude);
        
        EmergencyNotificationResponse response = emergencyService.triggerAudioEmergency(
            userId,
            latitude,
            longitude,
            location,
            audioSnippet,
            getAuthToken()
        );
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Emergency service is healthy");
    }

    private String getAuthToken() {
        // In a real implementation, this would get the service-to-service auth token
        // For now, return a placeholder
        return "service-token";
    }
} 