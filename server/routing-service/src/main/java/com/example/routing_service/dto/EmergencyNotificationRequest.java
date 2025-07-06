package com.example.routing_service.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class EmergencyNotificationRequest {
    private String userId;
    private String message;
    private double latitude;
    private double longitude;
    private String location;
    private String audioSnippet;
    private String emergencyType;
    private String reason;
    private List<String> emergencyContactIds;
} 