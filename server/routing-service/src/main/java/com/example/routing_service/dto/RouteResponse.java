package com.example.routing_service.dto;

import com.example.routing_service.model.Route;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteResponse {
    private String id;
    private String userId;
    private String journeyId;
    private double startLatitude;
    private double startLongitude;
    private double endLatitude;
    private double endLongitude;
    private List<Route.RoutePoint> routePoints;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Route.RouteStatus status;
    private int estimatedDurationMinutes;
    private double estimatedDistanceKm;
    
    public static RouteResponse fromRoute(Route route) {
        return new RouteResponse(
            route.getId(),
            route.getUserId(),
            route.getJourneyId(),
            route.getStartLatitude(),
            route.getStartLongitude(),
            route.getEndLatitude(),
            route.getEndLongitude(),
            route.getRoutePoints(),
            route.getCreatedAt(),
            route.getUpdatedAt(),
            route.getStatus(),
            route.getEstimatedDurationMinutes(),
            route.getEstimatedDistanceKm()
        );
    }
}
