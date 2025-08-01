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
import jakarta.servlet.http.HttpServletRequest;
import com.usermanagement_service.security.JwtService;
import org.springframework.security.access.AccessDeniedException;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserManagementService userManagementService;
    private final JwtService jwtService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @PostMapping
    public ResponseEntity<UserProfileResponse> createUserProfile(
            @RequestBody UserCreationRequest request,
            HttpServletRequest httpRequest) {
        // Extract token and verify user is creating their own profile
        String token = httpRequest.getHeader("Authorization").substring(7);
        String authenticatedUserId = jwtService.extractUserId(token);
        
        if (!authenticatedUserId.equals(request.getId())) {
            throw new AccessDeniedException("You can only create your own profile");
        }
        
        logger.info("Received request to create user profile: {}", request);
        try {
            UserProfileResponse response = userManagementService.createUserProfile(request);
            logger.info("User profile created successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error creating user profile: {}", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserProfileResponse> getUserProfile(
            @PathVariable String userId,
            HttpServletRequest request) {
        // Extract token and verify user is accessing their own data
        String token = request.getHeader("Authorization").substring(7);
        String authenticatedUserId = jwtService.extractUserId(token);
        
        if (!authenticatedUserId.equals(userId)) {
            throw new AccessDeniedException("You can only access your own profile");
        }
        
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
            @RequestBody UserUpdateRequest request,
            HttpServletRequest httpRequest) {
        // Extract token and verify user is updating their own data
        String token = httpRequest.getHeader("Authorization").substring(7);
        String authenticatedUserId = jwtService.extractUserId(token);
        
        if (!authenticatedUserId.equals(userId)) {
            throw new AccessDeniedException("You can only update your own profile");
        }
        
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
            @PathVariable String userId,
            HttpServletRequest request) {
        // Extract token and verify user is accessing their own contacts
        String token = request.getHeader("Authorization").substring(7);
        String authenticatedUserId = jwtService.extractUserId(token);
        
        if (!authenticatedUserId.equals(userId)) {
            throw new AccessDeniedException("You can only access your own emergency contacts");
        }
        
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
            @PathVariable String contactId,
            HttpServletRequest request) {
        // Extract token and verify user is removing their own contact
        String token = request.getHeader("Authorization").substring(7);
        String authenticatedUserId = jwtService.extractUserId(token);
        
        if (!authenticatedUserId.equals(userId)) {
            throw new AccessDeniedException("You can only remove contacts from your own profile");
        }
        
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

    @GetMapping("/search")
    public ResponseEntity<List<UserSearchResponse>> searchUsers(
            @RequestParam String query,
            HttpServletRequest request) {
        // Extract token and verify user is authenticated
        String token = request.getHeader("Authorization").substring(7);
        String authenticatedUserId = jwtService.extractUserId(token);
        
        logger.info("User {} searching for users with query: {}", authenticatedUserId, query);
        try {
            List<UserSearchResponse> users = userManagementService.searchUsers(query, authenticatedUserId);
            logger.info("Found {} users matching query: {}", users.size(), query);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error searching users", e);
            return ResponseEntity.badRequest().build();
        }
    }
} 