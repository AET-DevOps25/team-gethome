package com.example.routing_service.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "routes")
public class Route {
    @Id
    private String id;
    
    private String userId;
    
    private String journeyId;
    
    private double startLatitude;
    
    private double startLongitude;
    
    private double endLatitude;
    
    private double endLongitude;
    
    private List<RoutePoint> routePoints;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private RouteStatus status;
    
    private int estimatedDurationMinutes;
    
    private double estimatedDistanceKm;
    
    public Route(String userId, String journeyId, double startLatitude, double startLongitude, 
                 double endLatitude, double endLongitude) {
        this.userId = userId;
        this.journeyId = journeyId;
        this.startLatitude = startLatitude;
        this.startLongitude = startLongitude;
        this.endLatitude = endLatitude;
        this.endLongitude = endLongitude;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = RouteStatus.PLANNED;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoutePoint {
        private double latitude;
        private double longitude;
        private int sequenceNumber;
    }
    
    public enum RouteStatus {
        PLANNED, ACTIVE, COMPLETED, CANCELLED
    }
}
