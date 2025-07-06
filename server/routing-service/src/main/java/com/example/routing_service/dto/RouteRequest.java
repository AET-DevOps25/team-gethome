package com.example.routing_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteRequest {
    @NotNull(message = "Start location is required")
    private Location startLocation;
    
    @NotNull(message = "End location is required")
    private Location endLocation;
    
    private List<Location> waypoints;
    
    @DecimalMin(value = "0.0", message = "Safety preference must be between 0.0 and 1.0")
    @DecimalMax(value = "1.0", message = "Safety preference must be between 0.0 and 1.0")
    private Double safetyPreference = 0.8; // Default to high safety preference
    
    private String routeName;
    private String userId;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Location {
        @NotNull(message = "Latitude is required")
        @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
        @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
        private Double latitude;
        
        @NotNull(message = "Longitude is required")
        @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
        @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
        private Double longitude;
        
        private String address;
    }
} 