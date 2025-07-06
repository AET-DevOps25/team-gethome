package com.example.routing_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteResponse {
    private String routeId;
    private String routeName;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    
    private Location startLocation;
    private Location endLocation;
    private List<Location> waypoints;
    
    private double totalDistance; // in meters
    private int estimatedDuration; // in seconds
    private double safetyScore; // 0.0 to 1.0
    
    private List<RouteSegment> segments;
    private List<String> avoidedDangerZones;
    private String status;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Location {
        private double latitude;
        private double longitude;
        private String address;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RouteSegment {
        private Location start;
        private Location end;
        private double distance;
        private int duration;
        private String instructions;
        private List<Double[]> coordinates; // [[lat, lng], [lat, lng], ...]
    }
} 