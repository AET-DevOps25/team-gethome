package com.example.routing_service.service;

import com.example.routing_service.dto.RoutePlanRequest;
import com.example.routing_service.dto.RouteReplanRequest;
import com.example.routing_service.dto.RouteResponse;
import com.example.routing_service.model.Route;
import com.example.routing_service.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RouteService {
    
    private final RouteRepository routeRepository;
    private final FlagService flagService;
    private static final Logger logger = LoggerFactory.getLogger(RouteService.class);
    
    public RouteResponse planRoute(RoutePlanRequest request, String userId) {
        logger.info("Planning route for user: {} from ({}, {}) to ({}, {})", 
                   userId, request.getStartLatitude(), request.getStartLongitude(),
                   request.getEndLatitude(), request.getEndLongitude());
        
        String journeyId = request.getJourneyId();
        if (journeyId == null || journeyId.trim().isEmpty()) {
            journeyId = UUID.randomUUID().toString();
        }
        
        Route route = new Route(
            userId,
            journeyId,
            request.getStartLatitude(),
            request.getStartLongitude(),
            request.getEndLatitude(),
            request.getEndLongitude()
        );
        
        // Simulate route planning with flag avoidance
        List<Route.RoutePoint> routePoints = generateRoutePoints(
            request.getStartLatitude(), request.getStartLongitude(),
            request.getEndLatitude(), request.getEndLongitude(),
            request.getAvoidanceRadiusMeters()
        );
        
        route.setRoutePoints(routePoints);
        route.setEstimatedDurationMinutes(calculateEstimatedDuration(routePoints));
        route.setEstimatedDistanceKm(calculateEstimatedDistance(routePoints));
        
        Route savedRoute = routeRepository.save(route);
        logger.info("Route planned successfully with id: {}", savedRoute.getId());
        
        return RouteResponse.fromRoute(savedRoute);
    }
    
    public RouteResponse replanRoute(RouteReplanRequest request, String userId) {
        logger.info("Replanning route for journey: {} by user: {}", request.getJourneyId(), userId);
        
        Optional<Route> existingRoute = routeRepository.findByJourneyId(request.getJourneyId());
        
        if (existingRoute.isEmpty()) {
            throw new RuntimeException("Route not found for journey ID: " + request.getJourneyId());
        }
        
        Route route = existingRoute.get();
        
        // Verify the user owns this route
        if (!route.getUserId().equals(userId)) {
            throw new RuntimeException("You can only replan your own routes");
        }
        
        // Generate new route points avoiding flags
        List<Route.RoutePoint> newRoutePoints = generateRoutePoints(
            route.getStartLatitude(), route.getStartLongitude(),
            route.getEndLatitude(), route.getEndLongitude(),
            request.getAvoidanceRadiusMeters()
        );
        
        route.setRoutePoints(newRoutePoints);
        route.setEstimatedDurationMinutes(calculateEstimatedDuration(newRoutePoints));
        route.setEstimatedDistanceKm(calculateEstimatedDistance(newRoutePoints));
        route.setUpdatedAt(LocalDateTime.now());
        
        Route savedRoute = routeRepository.save(route);
        logger.info("Route replanned successfully for journey: {}", request.getJourneyId());
        
        return RouteResponse.fromRoute(savedRoute);
    }
    
    public RouteResponse getRoute(String journeyId, String userId) {
        logger.info("Getting route for journey: {} by user: {}", journeyId, userId);
        
        Optional<Route> route = routeRepository.findByJourneyId(journeyId);
        
        if (route.isEmpty()) {
            throw new RuntimeException("Route not found for journey ID: " + journeyId);
        }
        
        // Verify the user owns this route
        if (!route.get().getUserId().equals(userId)) {
            throw new RuntimeException("You can only access your own routes");
        }
        
        logger.info("Route retrieved successfully for journey: {}", journeyId);
        return RouteResponse.fromRoute(route.get());
    }
    
    private List<Route.RoutePoint> generateRoutePoints(double startLat, double startLon, 
                                                      double endLat, double endLon, 
                                                      double avoidanceRadius) {
        // This is a simplified route generation - in a real implementation,
        // you would integrate with a routing service like Google Maps, OpenRouteService, etc.
        // and check for flags along the route
        
        List<Route.RoutePoint> points = new ArrayList<>();
        
        // Add start point
        points.add(new Route.RoutePoint(startLat, startLon, 0));
        
        // Add some intermediate points (simplified linear interpolation)
        int numIntermediatePoints = 5;
        for (int i = 1; i <= numIntermediatePoints; i++) {
            double ratio = (double) i / (numIntermediatePoints + 1);
            double lat = startLat + (endLat - startLat) * ratio;
            double lon = startLon + (endLon - startLon) * ratio;
            points.add(new Route.RoutePoint(lat, lon, i));
        }
        
        // Add end point
        points.add(new Route.RoutePoint(endLat, endLon, numIntermediatePoints + 1));
        
        return points;
    }
    
    private int calculateEstimatedDuration(List<Route.RoutePoint> routePoints) {
        // Simplified duration calculation - in reality this would consider
        // traffic conditions, road types, speed limits, etc.
        double distance = calculateEstimatedDistance(routePoints);
        double averageSpeedKmh = 30.0; // Average city speed
        return (int) Math.ceil(distance / averageSpeedKmh * 60); // Convert to minutes
    }
    
    private double calculateEstimatedDistance(List<Route.RoutePoint> routePoints) {
        if (routePoints.size() < 2) {
            return 0.0;
        }
        
        double totalDistance = 0.0;
        for (int i = 1; i < routePoints.size(); i++) {
            Route.RoutePoint prev = routePoints.get(i - 1);
            Route.RoutePoint curr = routePoints.get(i);
            totalDistance += calculateDistance(
                prev.getLatitude(), prev.getLongitude(),
                curr.getLatitude(), curr.getLongitude()
            );
        }
        
        return totalDistance / 1000.0; // Convert to kilometers
    }
    
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Haversine formula
        final int EARTH_RADIUS = 6371000; // Earth's radius in meters
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS * c;
    }
}
