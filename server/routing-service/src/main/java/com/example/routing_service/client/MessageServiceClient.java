package com.example.routing_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "message-service", url = "${service.message.url}", configuration = com.example.routing_service.feignconfig.FeignConfig.class)
public interface MessageServiceClient {
    
    @PostMapping("/api/emergency/notify")
    EmergencyNotificationResponse sendEmergencyNotification(@RequestBody EmergencyNotificationRequest request);
    
    // DTOs for requests and responses
    record EmergencyNotificationRequest(String userId, 
                                       String message, 
                                       double latitude, 
                                       double longitude, 
                                       String audioSnippet,
                                       String emergencyType,
                                       String reason,
                                       String location,
                                       java.util.List<String> emergencyContactIds) {}
    
    record EmergencyNotificationResponse(String id, String status, String message) {}
} 