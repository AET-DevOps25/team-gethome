package com.example.routing_service.service;

import com.example.routing_service.client.OpenRouteServiceClient;
import com.example.routing_service.client.UserManagementClient;
import com.example.routing_service.dto.RouteRequest;
import com.example.routing_service.dto.RouteResponse;
import com.example.routing_service.model.DangerZone;
import com.example.routing_service.model.Route;
import com.example.routing_service.repository.DangerZoneRepository;
import com.example.routing_service.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoutingService {

    private final RouteRepository routeRepository;
    private final DangerZoneRepository dangerZoneRepository;
    private final OpenRouteServiceClient openRouteServiceClient;
    private final UserManagementClient userManagementClient;
    private final SafetyAnalysisService safetyAnalysisService;

    public RouteResponse planSafeRoute(RouteRequest request, String userId) {
        log.info("Planning safe route for user: {}", userId);
        
        // Get nearby danger zones
        List<DangerZone> nearbyDangerZones = getNearbyDangerZones(
            request.getStartLocation().getLatitude(),
            request.getStartLocation().getLongitude(),
            request.getEndLocation().getLatitude(),
            request.getEndLocation().getLongitude()
        );
        
        // Calculate route avoiding danger zones
        Route route = calculateSafeRoute(request, nearbyDangerZones, userId);
        
        // Save route
        route = routeRepository.save(route);
        
        return convertToResponse(route);
    }

    private List<DangerZone> getNearbyDangerZones(double startLat, double startLng, 
                                                  double endLat, double endLng) {
        // Get danger zones near start and end points (within 2km radius)
        List<DangerZone> startNearby = dangerZoneRepository.findNearbyActiveDangerZones(
            startLng, startLat, 2000, LocalDateTime.now());
        
        List<DangerZone> endNearby = dangerZoneRepository.findNearbyActiveDangerZones(
            endLng, endLat, 2000, LocalDateTime.now());
        
        // Combine and remove duplicates
        Set<String> seenIds = new HashSet<>();
        List<DangerZone> allNearby = new ArrayList<>();
        
        for (DangerZone zone : startNearby) {
            if (seenIds.add(zone.getId())) {
                allNearby.add(zone);
            }
        }
        
        for (DangerZone zone : endNearby) {
            if (seenIds.add(zone.getId())) {
                allNearby.add(zone);
            }
        }
        
        return allNearby;
    }

    private Route calculateSafeRoute(RouteRequest request, List<DangerZone> dangerZones, String userId) {
        try {
            // Prepare coordinates for OpenRouteService
            List<double[]> coordinates = new ArrayList<>();
            coordinates.add(new double[]{request.getStartLocation().getLongitude(), 
                                       request.getStartLocation().getLatitude()});
            
            if (request.getWaypoints() != null) {
                for (RouteRequest.Location waypoint : request.getWaypoints()) {
                    coordinates.add(new double[]{waypoint.getLongitude(), waypoint.getLatitude()});
                }
            }
            
            coordinates.add(new double[]{request.getEndLocation().getLongitude(), 
                                       request.getEndLocation().getLatitude()});
            
            // Call OpenRouteService
            OpenRouteServiceClient.OpenRouteRequest orsRequest = 
                new OpenRouteServiceClient.OpenRouteRequest(
                    coordinates.toArray(new double[0][]),
                    "recommended", // preference
                    "meters",
                    "en",
                    "true",
                    false
                );
            
            OpenRouteServiceClient.OpenRouteResponse orsResponse = 
                openRouteServiceClient.getWalkingRoute(orsRequest, "Bearer " + System.getenv("OPENROUTE_API_KEY"));
            
            // Convert response to our Route model
            Route route = convertOpenRouteResponse(orsResponse, request, userId);
            
            // Calculate safety score based on danger zones
            double safetyScore = safetyAnalysisService.calculateSafetyScore(route, dangerZones);
            route.setSafetyScore(safetyScore);
            
            // Add avoided danger zones
            List<String> avoidedZones = dangerZones.stream()
                .map(DangerZone::getId)
                .collect(Collectors.toList());
            route.setAvoidedDangerZones(avoidedZones);
            
            return route;
            
        } catch (Exception e) {
            log.error("Error calculating route: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to calculate route", e);
        }
    }

    private Route convertOpenRouteResponse(OpenRouteServiceClient.OpenRouteResponse orsResponse, 
                                         RouteRequest request, String userId) {
        if (orsResponse.features() == null || orsResponse.features().length == 0) {
            throw new RuntimeException("No route found");
        }
        
        OpenRouteServiceClient.OpenRouteFeature feature = orsResponse.features()[0];
        OpenRouteServiceClient.OpenRouteProperties properties = feature.properties();
        
        Route route = Route.builder()
            .userId(userId)
            .routeName(request.getRouteName() != null ? request.getRouteName() : "Safe Route")
            .createdAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusHours(24))
            .startLocation(convertLocation(request.getStartLocation()))
            .endLocation(convertLocation(request.getEndLocation()))
            .totalDistance(properties.distance())
            .estimatedDuration((int) properties.duration())
            .status(Route.RouteStatus.ACTIVE)
            .build();
        
        // Convert segments
        List<Route.RouteSegment> segments = new ArrayList<>();
        for (OpenRouteServiceClient.OpenRouteSegment orsSegment : properties.segments()) {
            double[][] wayPoints = orsSegment.way_points();
            List<Double[]> coordinates = new ArrayList<>();
            for (double[] point : wayPoints) {
                Double[] boxed = Arrays.stream(point).boxed().toArray(Double[]::new);
                coordinates.add(boxed);
            }
            Route.RouteSegment segment = Route.RouteSegment.builder()
                .distance(orsSegment.distance())
                .duration((int) orsSegment.duration())
                .instructions(orsSegment.instruction())
                .coordinates(coordinates)
                .build();
            segments.add(segment);
        }
        route.setSegments(segments);
        
        return route;
    }

    private Route.Location convertLocation(RouteRequest.Location requestLocation) {
        return Route.Location.builder()
            .latitude(requestLocation.getLatitude())
            .longitude(requestLocation.getLongitude())
            .address(requestLocation.getAddress())
            .build();
    }

    private RouteResponse convertToResponse(Route route) {
        return RouteResponse.builder()
            .routeId(route.getId())
            .routeName(route.getRouteName())
            .createdAt(route.getCreatedAt())
            .expiresAt(route.getExpiresAt())
            .startLocation(convertToResponseLocation(route.getStartLocation()))
            .endLocation(convertToResponseLocation(route.getEndLocation()))
            .totalDistance(route.getTotalDistance())
            .estimatedDuration(route.getEstimatedDuration())
            .safetyScore(route.getSafetyScore())
            .segments(convertToResponseSegments(route.getSegments()))
            .avoidedDangerZones(route.getAvoidedDangerZones())
            .status(route.getStatus().name())
            .build();
    }

    private RouteResponse.Location convertToResponseLocation(Route.Location location) {
        return RouteResponse.Location.builder()
            .latitude(location.getLatitude())
            .longitude(location.getLongitude())
            .address(location.getAddress())
            .build();
    }

    private List<RouteResponse.RouteSegment> convertToResponseSegments(List<Route.RouteSegment> segments) {
        return segments.stream()
            .map(segment -> RouteResponse.RouteSegment.builder()
                .distance(segment.getDistance())
                .duration(segment.getDuration())
                .instructions(segment.getInstructions())
                .coordinates(segment.getCoordinates())
                .build())
            .collect(Collectors.toList());
    }

    public List<RouteResponse> getUserRoutes(String userId) {
        List<Route> routes = routeRepository.findByUserIdAndStatus(userId, Route.RouteStatus.ACTIVE);
        return routes.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    public RouteResponse getRoute(String routeId, String userId) {
        Route route = routeRepository.findById(routeId)
            .orElseThrow(() -> new RuntimeException("Route not found"));
        
        if (!route.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        
        return convertToResponse(route);
    }

    public void completeRoute(String routeId, String userId) {
        Route route = routeRepository.findById(routeId)
            .orElseThrow(() -> new RuntimeException("Route not found"));
        
        if (!route.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        
        route.setStatus(Route.RouteStatus.COMPLETED);
        routeRepository.save(route);
    }
} 