package com.message_service.controller;

import com.message_service.dto.EmergencyNotificationRequest;
import com.message_service.dto.JourneyStartRequest;
import com.message_service.dto.JourneyEndRequest;
import com.message_service.security.JwtService;
import com.message_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final NotificationService notificationService;
    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);
    private final JwtService jwtService;

    @PostMapping("{userId}/journey-start")
    public ResponseEntity<Void> notifyJourneyStart(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody JourneyStartRequest request,
            @PathVariable String userId
    ) {
        String token = authHeader.substring(7);
        String authenticatedUserId = jwtService.extractUserId(token);

        if (!authenticatedUserId.equals(userId)) {
            throw new AccessDeniedException("You can only create your own profile");
        }

        boolean success = notificationService.notifyJourneyStart(authHeader, userId, request);
        if (!success) {
            logger.error("Failed to notify journey start for user {}", userId);
            return ResponseEntity.status(500).build();
        }
        logger.info("User {} started a journey.", userId);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("{userId}/journey-end")
    public ResponseEntity<Void> notifyJourneyEnd(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody JourneyEndRequest request,
            @PathVariable String userId
    ) {
        String token = authHeader.substring(7);
        String authenticatedUserId = jwtService.extractUserId(token);
        if (!authenticatedUserId.equals(userId)) {
            throw new AccessDeniedException("You can only create your own profile");
        }
        boolean success = notificationService.notifyJourneyEnd(authHeader, userId, request);
        if (!success) {
            logger.error("Failed to notify journey end for user {}", userId);
            return ResponseEntity.status(500).build();
        }
        logger.info("User {} ended a journey.", userId);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("{userId}/emergency")
    public ResponseEntity<Void> notifyEmergency(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody EmergencyNotificationRequest request,
            @PathVariable String userId
    ) {
        String token = authHeader.substring(7);
        String authenticatedUserId = jwtService.extractUserId(token);
        if (!authenticatedUserId.equals(userId)) {
            throw new AccessDeniedException("You can only create your own profile");
        }
        boolean success = notificationService.notifyEmergency(authHeader, userId, request);
        if (!success) {
            logger.error("Failed to notify emergency for user {}", userId);
            return ResponseEntity.status(500).build();
        }
        logger.info("User {} sent an emergency notification.", userId);
        return ResponseEntity.accepted().build();
    }
}
