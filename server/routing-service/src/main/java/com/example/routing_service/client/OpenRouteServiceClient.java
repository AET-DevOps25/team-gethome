package com.example.routing_service.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "openrouteservice", url = "${routing.api.url}", configuration = com.example.routing_service.feignconfig.OpenRouteServiceConfig.class)
public interface OpenRouteServiceClient {
    
    @PostMapping("/directions/foot-walking")
    OpenRouteResponse getWalkingRoute(@RequestBody OpenRouteRequest request);
    
    // DTOs for OpenRouteService API
    record OpenRouteRequest(double[][] coordinates, 
                           String preference, 
                           String units, 
                           String language, 
                           String geometry_simplify,
                           boolean continue_straight,
                           @JsonInclude(JsonInclude.Include.NON_NULL)
                           Options options) {}

    record Options(AvoidPolygons avoid_polygons) {}
    record AvoidPolygons(String type, Object coordinates) {}
    
    record OpenRouteResponse(OpenRouteRoute[] routes, 
                            OpenRouteMetadata metadata) {}
    
    record OpenRouteRoute(OpenRouteSummary summary, 
                         OpenRouteSegment[] segments,
                         String geometry,
                         int[] way_points) {}
    
    record OpenRouteSummary(double distance, double duration) {}
    
    record OpenRouteSegment(double distance, 
                           double duration, 
                           OpenRouteStep[] steps) {}
    
    record OpenRouteStep(double distance, 
                        double duration, 
                        String instruction, 
                        String name,
                        int[] way_points) {}
    
    record OpenRouteMetadata(String attribution, 
                            String service, 
                            long timestamp, 
                            OpenRouteQuery query) {}
    
    record OpenRouteQuery(double[][] coordinates, 
                         String profile, 
                         String preference, 
                         String units, 
                         String language) {}
} 