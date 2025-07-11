package com.example.routing_service.service;

import com.example.routing_service.client.OpenRouteServiceClient;
import com.example.routing_service.client.OpenRouteServiceClient.Options;
import com.example.routing_service.client.OpenRouteServiceClient.AvoidPolygons;
import com.example.routing_service.client.UserManagementClient;
import com.example.routing_service.dto.RouteRequest;
import com.example.routing_service.dto.RouteResponse;
import com.example.routing_service.model.DangerZone;
import com.example.routing_service.model.Route;
import com.example.routing_service.repository.DangerZoneRepository;
import com.example.routing_service.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// Custom metrics imports
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.DistributionSummary;
import jakarta.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoutingService {

    private final RouteRepository routeRepository;
    private final DangerZoneRepository dangerZoneRepository;
    private final OpenRouteServiceClient openRouteServiceClient;
    private final UserManagementClient userManagementClient;
    private final SafetyAnalysisService safetyAnalysisService;
    private final MeterRegistry meterRegistry;
    
    @Value("${routing.api.key}")
    private String openRouteApiKey;

    // Custom business metrics
    private Counter routeRequestsTotal;
    private Counter safeRoutesGenerated;
    private Counter dangerZonesAvoided;
    private Counter emergencyRoutesRequested;
    private Timer routeCalculationTime;
    private Timer openRouteServiceResponseTime;
    private DistributionSummary routeDistanceDistribution;
    private DistributionSummary safetScoreDistribution;
    private Gauge activeDangerZones;
    private Gauge averageRouteDistance;
    private Gauge averageSafetyScore;
    private Counter routeOptimizationSuccess;
    private Counter routeOptimizationFailures;
    private Timer userSafetyAnalysisTime;
    
    // Business KPI tracking
    private final AtomicInteger totalActiveRoutes = new AtomicInteger(0);
    private final AtomicLong totalDistancePlanned = new AtomicLong(0);
    private final AtomicInteger totalDangerZonesInSystem = new AtomicInteger(0);

    @PostConstruct
    public void initializeMetrics() {
        // Route planning metrics
        routeRequestsTotal = Counter.builder("gethome_route_requests_total")
                .description("Total number of route planning requests")
                .tag("service", "routing")
                .register(meterRegistry);

        safeRoutesGenerated = Counter.builder("gethome_safe_routes_generated_total")
                .description("Total number of successfully generated safe routes")
                .tag("service", "routing")
                .register(meterRegistry);

        dangerZonesAvoided = Counter.builder("gethome_danger_zones_avoided_total")
                .description("Total number of danger zones successfully avoided in routes")
                .tag("service", "routing")
                .register(meterRegistry);

        emergencyRoutesRequested = Counter.builder("gethome_emergency_routes_total")
                .description("Total number of emergency route requests")
                .tag("service", "routing")
                .tag("priority", "high")
                .register(meterRegistry);

        // Performance metrics
        routeCalculationTime = Timer.builder("gethome_route_calculation_duration_seconds")
                .description("Time taken to calculate routes including safety analysis")
                .tag("service", "routing")
                .register(meterRegistry);

        openRouteServiceResponseTime = Timer.builder("gethome_openroute_response_duration_seconds")
                .description("Response time from OpenRoute Service API")
                .tag("service", "routing")
                .tag("external_api", "openroute")
                .register(meterRegistry);

        userSafetyAnalysisTime = Timer.builder("gethome_safety_analysis_duration_seconds")
                .description("Time taken to analyze route safety and danger zones")
                .tag("service", "routing")
                .tag("feature", "safety")
                .register(meterRegistry);

        // Business intelligence metrics
        routeDistanceDistribution = DistributionSummary.builder("gethome_route_distance_meters")
                .description("Distribution of planned route distances in meters")
                .tag("service", "routing")
                .register(meterRegistry);

        safetScoreDistribution = DistributionSummary.builder("gethome_route_safety_score")
                .description("Distribution of route safety scores (0.0 to 1.0)")
                .tag("service", "routing")
                .tag("feature", "safety")
                .register(meterRegistry);

        // System health metrics
        activeDangerZones = Gauge.builder("gethome_active_danger_zones_count", totalDangerZonesInSystem, AtomicInteger::get)
                .description("Current number of active danger zones in the system")
                .tag("service", "routing")
                .tag("feature", "safety")
                .register(meterRegistry);

        averageRouteDistance = Gauge.builder("gethome_average_route_distance_meters", this, RoutingService::calculateAverageRouteDistance)
                .description("Average distance of all planned routes")
                .tag("service", "routing")
                .register(meterRegistry);

        averageSafetyScore = Gauge.builder("gethome_average_safety_score", this, RoutingService::calculateAverageSafetyScore)
                .description("Average safety score across all routes")
                .tag("service", "routing")
                .tag("feature", "safety")
                .register(meterRegistry);

        // Success/failure tracking
        routeOptimizationSuccess = Counter.builder("gethome_route_optimization_success_total")
                .description("Successful route optimizations for safety")
                .tag("service", "routing")
                .tag("optimization", "safety")
                .register(meterRegistry);

        routeOptimizationFailures = Counter.builder("gethome_route_optimization_failures_total")
                .description("Failed route optimization attempts")
                .tag("service", "routing")
                .tag("optimization", "safety")
                .register(meterRegistry);

        log.info("Custom GetHome routing metrics initialized successfully");
    }

    public RouteResponse planSafeRoute(RouteRequest request, String userId) throws Exception {
        // Increment request counter
        routeRequestsTotal.increment();
        
        // Track if this is an emergency route
        boolean isEmergencyRoute = request.getSafetyPreference() != null && request.getSafetyPreference() > 0.9;
        if (isEmergencyRoute) {
            emergencyRoutesRequested.increment();
        }

        try {
            return routeCalculationTime.recordCallable(() -> {
                log.info("Planning safe route for user: {} (Emergency: {})", userId, isEmergencyRoute);
                
                // Get nearby danger zones with safety analysis timing
                List<DangerZone> nearbyDangerZones = userSafetyAnalysisTime.recordCallable(() -> {
                    List<DangerZone> zones = getNearbyDangerZones(
            request.getStartLocation().getLatitude(),
            request.getStartLocation().getLongitude(),
            request.getEndLocation().getLatitude(),
            request.getEndLocation().getLongitude()
        );
                    
                    // Update danger zones count
                    totalDangerZonesInSystem.set(dangerZoneRepository.countActiveDangerZones(LocalDateTime.now()).intValue());
                    
                    // Track danger zones avoided
                    if (!zones.isEmpty()) {
                        dangerZonesAvoided.increment(zones.size());
                    }
                    
                    return zones;
                });
        
        // Calculate route avoiding danger zones
        Route route = calculateSafeRoute(request, nearbyDangerZones, userId);
                
                // Track successful route generation
                safeRoutesGenerated.increment();
                routeOptimizationSuccess.increment();
                
                // Record business metrics
                routeDistanceDistribution.record(route.getTotalDistance());
                safetScoreDistribution.record(route.getSafetyScore());
                totalDistancePlanned.addAndGet((long) route.getTotalDistance());
                totalActiveRoutes.incrementAndGet();
        
        // Save route
        route = routeRepository.save(route);
        
                log.info("Safe route generated successfully - Distance: {}m, Safety Score: {}, Danger Zones Avoided: {}", 
                        route.getTotalDistance(), route.getSafetyScore(), nearbyDangerZones.size());
                
        return convertToResponse(route);
            });
        } catch (Exception e) {
            routeOptimizationFailures.increment();
            log.error("Failed to generate safe route for user: {}", userId, e);
            throw e;
        }
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
            
            // Build avoid_polygons MultiPolygon for all danger zones (50m radius)
            List<double[][]> polygons = new ArrayList<>();
            for (DangerZone zone : dangerZones) {
                double lon = zone.getLocation().getX();
                double lat = zone.getLocation().getY();
                polygons.add(createCirclePolygon(lon, lat, 50, 16));
            }
            
            Options options = null;
            if (!polygons.isEmpty()) {
                // Always use MultiPolygon format for consistency, even for single danger zone
                    double[][][][] multiPoly = new double[polygons.size()][][][];
                    for (int i = 0; i < polygons.size(); i++) {
                        // Wrap each polygon in an additional array level
                        multiPoly[i] = new double[][][] { polygons.get(i) };
                    }
                AvoidPolygons avoidPolygons = new AvoidPolygons("MultiPolygon", multiPoly);
                options = new Options(avoidPolygons);
                log.info("Created MultiPolygon with {} danger zones", dangerZones.size());
            } else {
                log.info("No danger zones to avoid - proceeding without avoidance");
            }

            // Call OpenRouteService with timing
            OpenRouteServiceClient.OpenRouteRequest orsRequest = 
                new OpenRouteServiceClient.OpenRouteRequest(
                    coordinates.toArray(new double[0][]),
                    "recommended", // preference
                    "m", // units (meters)
                    "en",
                    "true",
                    false,
                    options
                );
            
            log.info("Sending request to OpenRouteService with coordinates: {} and avoid_polygons: {}", 
                    Arrays.deepToString(coordinates.toArray(new double[0][])), polygons.size());
            
            if (options != null && options.avoid_polygons() != null) {
                log.info("Avoid polygons structure: type={}, coordinates_count={}", 
                        options.avoid_polygons().type(), polygons.size());
            }
            
            // Log the full JSON request body for debugging
            try {
                ObjectMapper mapper = new ObjectMapper();
                String jsonRequest = mapper.writeValueAsString(orsRequest);
                log.info("ORS REQUEST JSON: {}", jsonRequest);
            } catch (Exception ex) {
                log.error("Failed to serialize ORS request", ex);
            }
            
            // Time the external API call
            OpenRouteServiceClient.OpenRouteResponse orsResponse = openRouteServiceResponseTime.recordCallable(() -> 
                openRouteServiceClient.getWalkingRoute(orsRequest)
            );
            
            log.info("OpenRouteService response received: {}", orsResponse);
            
            // Debug: Print the raw response structure
            if (orsResponse.routes() != null && orsResponse.routes().length > 0) {
                OpenRouteServiceClient.OpenRouteRoute route = orsResponse.routes()[0];
                log.info("Route structure - summary: {}, segments: {}", 
                        route.summary(), 
                        route.segments() != null ? route.segments().length : "null");
                
                if (route.segments() != null) {
                    for (int i = 0; i < route.segments().length; i++) {
                        log.info("Segment {}: {}", i, route.segments()[i]);
                    }
                }
            }
            
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
        log.info("Converting OpenRouteService response: routes={}, metadata={}", 
                orsResponse.routes() != null ? orsResponse.routes().length : "null",
                orsResponse.metadata());
        
        if (orsResponse.routes() == null || orsResponse.routes().length == 0) {
            log.error("No route found in OpenRouteService response. Full response: {}", orsResponse);
            throw new RuntimeException("No route found - the distance might be too far for walking or no walking route is available");
        }
        
        OpenRouteServiceClient.OpenRouteRoute route = orsResponse.routes()[0];
        OpenRouteServiceClient.OpenRouteSummary summary = route.summary();
        
        log.info("Processing route with summary: distance={}, duration={}", 
                summary != null ? summary.distance() : "null", 
                summary != null ? summary.duration() : "null");
        
        if (summary == null) {
            log.error("Route summary is null in OpenRouteService response. Route: {}", route);
            throw new RuntimeException("Invalid route response - missing summary information");
        }
        
        Route routeModel = Route.builder()
            .userId(userId)
            .routeName(request.getRouteName() != null ? request.getRouteName() : "Safe Route")
            .createdAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusHours(24))
            .startLocation(convertLocation(request.getStartLocation()))
            .endLocation(convertLocation(request.getEndLocation()))
            .totalDistance(summary.distance())
            .estimatedDuration((int) summary.duration())
            .status(Route.RouteStatus.ACTIVE)
            .build();
        
        log.info("Created route model with distance={}, duration={}", 
                routeModel.getTotalDistance(), routeModel.getEstimatedDuration());
        
        // Convert segments
        List<Route.RouteSegment> segments = new ArrayList<>();
        if (route.segments() != null) {
            log.info("Processing {} segments", route.segments().length);
            
            // Decode the geometry to get actual coordinates
            List<double[]> routeCoordinates = decodeGeometry(route.geometry());
            log.info("Decoded {} route coordinates", routeCoordinates.size());
            
            if (route.segments().length > 0) {
                OpenRouteServiceClient.OpenRouteSegment orsSegment = route.segments()[0];
                
                if (orsSegment.steps() != null && orsSegment.steps().length > 0) {
                    log.info("Processing {} steps with turn-by-turn instructions", orsSegment.steps().length);
                    
                    // Create segments for each step with proper instructions
                    for (int i = 0; i < orsSegment.steps().length; i++) {
                        OpenRouteServiceClient.OpenRouteStep orsStep = orsSegment.steps()[i];
                        
                        // Get coordinates for this step using way_points
                        List<Route.Location> stepCoordinates = new ArrayList<>();
                        
                        if (orsStep.way_points() != null && orsStep.way_points().length >= 2) {
                            int startIndex = orsStep.way_points()[0];
                            int endIndex = orsStep.way_points()[1];
                            
                            // Get coordinates for this step segment
                            for (int j = startIndex; j <= endIndex && j < routeCoordinates.size(); j++) {
                                double[] coord = routeCoordinates.get(j);
                                Route.Location location = Route.Location.builder()
                                    .latitude(coord[0])
                                    .longitude(coord[1])
                                    .build();
                                stepCoordinates.add(location);
                            }
                        }
                        
                        // If no coordinates found for this step, use a subset of all coordinates
                        if (stepCoordinates.isEmpty() && !routeCoordinates.isEmpty()) {
                            int stepSize = routeCoordinates.size() / orsSegment.steps().length;
                            int startIdx = i * stepSize;
                            int endIdx = Math.min(startIdx + stepSize, routeCoordinates.size());
                            
                            for (int j = startIdx; j < endIdx; j++) {
                                double[] coord = routeCoordinates.get(j);
                                Route.Location location = Route.Location.builder()
                                    .latitude(coord[0])
                                    .longitude(coord[1])
                                    .build();
                                stepCoordinates.add(location);
                            }
                        }
                        
                        // Create segment for this step
                        Route.Location startLoc = stepCoordinates.isEmpty() ? null : stepCoordinates.get(0);
                        Route.Location endLoc = stepCoordinates.isEmpty() ? null : stepCoordinates.get(stepCoordinates.size() - 1);
                        Route.RouteSegment segment = Route.RouteSegment.builder()
                            .start(startLoc)
                            .end(endLoc)
                            .distance(orsStep.distance())
                            .duration((int) orsStep.duration())
                            .instructions(orsStep.instruction() != null ? orsStep.instruction() : "Continue")
                            .coordinates(stepCoordinates)
                            .build();
                        segments.add(segment);
                        
                        log.info("Added step {}: {} ({} coordinates)", 
                                i, orsStep.instruction(), stepCoordinates.size());
                    }
                } else {
                    // Fallback: create one segment with all coordinates
                    List<Route.Location> allCoordinates = new ArrayList<>();
                    for (double[] coord : routeCoordinates) {
                        Route.Location location = Route.Location.builder()
                            .latitude(coord[0])
                            .longitude(coord[1])
                            .build();
                        allCoordinates.add(location);
                    }
                    
                    Route.Location startLoc = allCoordinates.isEmpty() ? null : allCoordinates.get(0);
                    Route.Location endLoc = allCoordinates.isEmpty() ? null : allCoordinates.get(allCoordinates.size() - 1);
                    Route.RouteSegment segment = Route.RouteSegment.builder()
                        .start(startLoc)
                        .end(endLoc)
                        .distance(summary.distance())
                        .duration((int) summary.duration())
                        .instructions("Follow the safe walking route")
                        .coordinates(allCoordinates)
                        .build();
                    segments.add(segment);
                    
                    log.info("Created fallback route with {} coordinates", allCoordinates.size());
                }
            }
        } else {
            log.warn("Route has null segments");
        }
        routeModel.setSegments(segments);
        
        log.info("Final route model created with {} segments", segments.size());
        return routeModel;
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
                .coordinates(segment.getCoordinates().stream()
                    .map(loc -> RouteResponse.Location.builder()
                        .latitude(loc.getLatitude())
                        .longitude(loc.getLongitude())
                        .address(loc.getAddress())
                        .build())
                    .collect(Collectors.toList()))
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

    private List<double[]> decodeGeometry(String encoded) {
        List<double[]> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            double latitude = lat / 1E5;
            double longitude = lng / 1E5;
            poly.add(new double[]{latitude, longitude});
        }
        return poly;
    }

    // Helper to create a circle polygon (GeoJSON ring) around a point
    private double[][] createCirclePolygon(double lon, double lat, double radiusMeters, int numPoints) {
        double[][] coords = new double[numPoints + 1][2];
        double earthRadius = 6371000.0;
        double d = radiusMeters / earthRadius;
        double latRad = Math.toRadians(lat);
        double lonRad = Math.toRadians(lon);
        
        for (int i = 0; i <= numPoints; i++) {
            double angle = 2 * Math.PI * i / numPoints;
            double lat2 = Math.asin(Math.sin(latRad) * Math.cos(d) + Math.cos(latRad) * Math.sin(d) * Math.cos(angle));
            double lon2 = lonRad + Math.atan2(Math.sin(angle) * Math.sin(d) * Math.cos(latRad), Math.cos(d) - Math.sin(latRad) * Math.sin(lat2));
            coords[i][0] = Math.toDegrees(lon2); // longitude
            coords[i][1] = Math.toDegrees(lat2); // latitude
        }
        
        // Ensure the polygon is closed (first and last point are identical)
        coords[numPoints][0] = coords[0][0];
        coords[numPoints][1] = coords[0][1];
        
        return coords;
    }

    // Business intelligence calculation methods
    private double calculateAverageRouteDistance() {
        try {
            return routeRepository.findAll().stream()
                    .mapToDouble(Route::getTotalDistance)
                    .average()
                    .orElse(0.0);
        } catch (Exception e) {
            log.warn("Failed to calculate average route distance", e);
            return 0.0;
        }
    }

    private double calculateAverageSafetyScore() {
        try {
            return routeRepository.findAll().stream()
                    .mapToDouble(Route::getSafetyScore)
                    .average()
                    .orElse(0.0);
        } catch (Exception e) {
            log.warn("Failed to calculate average safety score", e);
            return 0.0;
        }
    }
} 