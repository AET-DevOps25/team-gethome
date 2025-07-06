package com.example.gethome.message.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "usermanagement-service", url = "${service.usermanagement.url}")
public interface UserManagementClient {
    
    @GetMapping("/api/users/{userId}/profile")
    UserProfileResponse getUserProfile(@PathVariable String userId, 
                                     @RequestHeader("Authorization") String authorization);
    
    @GetMapping("/api/users/{userId}/emergency-contacts")
    EmergencyContactsResponse getEmergencyContacts(@PathVariable String userId,
                                                  @RequestHeader("Authorization") String authorization);
    
    // DTOs for responses
    record UserProfileResponse(String userId, String alias, String phoneNr, String profilePictureUrl) {}
    
    record EmergencyContactsResponse(String userId, java.util.List<EmergencyContact> emergencyContacts) {}
    
    record EmergencyContact(String id, String name, String email, String phone, String preferredMethod) {}
} 