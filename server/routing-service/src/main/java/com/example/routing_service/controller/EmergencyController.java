package com.example.routing_service.controller;

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
    public ResponseEntity<Void> triggerEmergency(@RequestParam double latitude,
                                                @RequestParam double longitude,
                                                @RequestParam(required = false) String audioSnippet,
                                                @RequestParam(required = false) String reason,
                                                Authentication authentication) {
        String userId = authentication.getName();
        log.warn("Emergency triggered by user: {} at location ({}, {})", userId, latitude, longitude);
        
        String emergencyReason = reason != null ? reason : "Manual emergency trigger";
        emergencyService.handleEmergency(userId, latitude, longitude, audioSnippet, emergencyReason);
        
        return ResponseEntity.ok().build();
    }

    @PostMapping("/audio")
    public ResponseEntity<Void> audioEmergency(@RequestParam double latitude,
                                              @RequestParam double longitude,
                                              @RequestParam String audioSnippet,
                                              Authentication authentication) {
        String userId = authentication.getName();
        log.warn("Audio emergency triggered by user: {} at location ({}, {})", userId, latitude, longitude);
        
        emergencyService.handleAudioEmergency(userId, latitude, longitude, audioSnippet);
        
        return ResponseEntity.ok().build();
    }

    @PostMapping("/manual")
    public ResponseEntity<Void> manualEmergency(@RequestParam double latitude,
                                               @RequestParam double longitude,
                                               Authentication authentication) {
        String userId = authentication.getName();
        log.warn("Manual emergency button pressed by user: {} at location ({}, {})", userId, latitude, longitude);
        
        emergencyService.handleManualEmergency(userId, latitude, longitude);
        
        return ResponseEntity.ok().build();
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Emergency service is healthy");
    }
} 