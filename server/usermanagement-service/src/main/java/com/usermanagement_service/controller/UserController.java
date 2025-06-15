package com.usermanagement_service.controller;

import com.usermanagement_service.dto.*;
import com.usermanagement_service.model.EmergencyContact;
import com.usermanagement_service.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserManagementService userManagementService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @PostMapping
    public ResponseEntity<UserProfileResponse> createUserProfile(@RequestBody UserCreationRequest request) {
        logger.info("Received request to create user profile: {}", request);
        try {
            UserProfileResponse profile = userManagementService.createUserProfile(request);
            logger.info("User profile created successfully");
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            logger.error("Error creating user profile", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable String userId) {
        logger.info("Received request for user profile with userId: {}", userId);
        try {
            UserProfileResponse profile = userManagementService.getUserProfile(userId);
            logger.info("Successfully retrieved user profile for userId: {}", userId);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            logger.error("Error retrieving profile for userId: {}", userId, e);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> updateUserProfile(
            @PathVariable String userId,
            @RequestBody UserUpdateRequest request) {
        logger.info("Received request to update user profile for userId: {}", userId);
        try {
            UserProfileResponse profile = userManagementService.updateUserProfile(userId, request);
            logger.info("User profile updated successfully");
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            logger.error("Error updating user profile", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{requesterId}/emergency-contacts/{contactUserId}")
    public ResponseEntity<AddEmergencyContactResponse> addEmergencyContact(
            @PathVariable String requesterId,
            @PathVariable String contactUserId) {
        logger.info("Received request to add emergency contact for userId: {} and contactUserId: {}", requesterId, contactUserId);
        try {
            AddEmergencyContactResponse response = userManagementService.addEmergencyContact(requesterId, contactUserId);
            logger.info("Emergency contact added successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error adding emergency contact", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{userId}/emergency-contacts/pending")
    public ResponseEntity<List<EmergencyContact>> getPendingEmergencyContactRequests(
            @PathVariable String userId) {
        return ResponseEntity.ok(userManagementService.getPendingEmergencyContactRequests(userId));
    }

    @PutMapping("/{userId}/emergency-contacts/{requestId}")
    public ResponseEntity<Void> respondToEmergencyContactRequest(
            @PathVariable String userId,
            @PathVariable String requestId,
            @RequestParam boolean accept) {
        userManagementService.respondToEmergencyContactRequest(requestId, accept);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/emergency-contacts")
    public ResponseEntity<List<EmergencyContact>> getEmergencyContacts(
            @PathVariable String userId) {
        return ResponseEntity.ok(userManagementService.getEmergencyContacts(userId));
    }

    @GetMapping("/{userId}/emergency-contacts-of")
    public ResponseEntity<List<EmergencyContact>> getEmergencyContactsOf(
            @PathVariable String userId) {
        return ResponseEntity.ok(userManagementService.getEmergencyContactsOf(userId));
    }

    @DeleteMapping("/{userId}/emergency-contacts/{contactId}")
    public ResponseEntity<Void> removeEmergencyContact(
            @PathVariable String userId,
            @PathVariable String contactId) {
        userManagementService.removeEmergencyContact(userId, contactId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}/emergency-contacts-of/{requesterId}")
    public ResponseEntity<Void> removeEmergencyContactOf(
            @PathVariable String userId,
            @PathVariable String requesterId) {
        userManagementService.removeEmergencyContactOf(requesterId, userId);
        return ResponseEntity.noContent().build();
    }
} 