package com.example.gethome.message.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "usermanagement-service", url = "${service.usermanagement.url}", configuration = com.example.gethome.message.config.FeignConfig.class)
public interface UserManagementClient {
    
    @GetMapping("/api/users/{userId}/profile")
    UserProfileResponse getUserProfile(@PathVariable String userId);
    
    @GetMapping("/api/users/{userId}/emergency-contacts")
    List<EmergencyContact> getEmergencyContacts(@PathVariable String userId);
    
    // DTOs for responses
    record UserProfileResponse(String userId, String email, String alias, String phoneNr, String profilePictureUrl) {}
    
    record EmergencyContact(String id, String name, String email, String phone, String preferredMethod) {}
} 