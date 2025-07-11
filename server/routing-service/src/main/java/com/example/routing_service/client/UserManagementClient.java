package com.example.routing_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "usermanagement-service", url = "${service.usermanagement.url}", configuration = com.example.routing_service.feignconfig.FeignConfig.class)
public interface UserManagementClient {
    
    @GetMapping("/api/users/{userId}/profile")
    UserProfileResponse getUserProfile(@PathVariable String userId);
    
    @GetMapping("/api/users/{userId}/emergency-contacts")
    List<EmergencyContact> getEmergencyContacts(@PathVariable String userId);
    
    // DTOs for responses
    record UserProfileResponse(String userId, String alias, String phoneNr, String profilePictureUrl) {}
    
    // DTO for emergency contact
    record EmergencyContact(String id, String name, String phone, String relationship) {}
} 