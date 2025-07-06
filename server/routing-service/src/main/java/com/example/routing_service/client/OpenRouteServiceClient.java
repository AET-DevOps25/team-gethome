package com.example.routing_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "openrouteservice", url = "${routing.api.url}")
public interface OpenRouteServiceClient {
    
    @PostMapping("/directions/foot-walking")
    OpenRouteResponse getWalkingRoute(@RequestBody OpenRouteRequest request,
                                     @RequestHeader("Authorization") String apiKey);
    
    // DTOs for OpenRouteService API
    record OpenRouteRequest(double[][] coordinates, 
                           String preference, 
                           String units, 
                           String language, 
                           String geometry_simplify,
                           boolean continue_straight) {}
    
    record OpenRouteResponse(OpenRouteFeature[] features, 
                            OpenRouteMetadata metadata) {}
    
    record OpenRouteFeature(OpenRouteGeometry geometry, 
                           OpenRouteProperties properties, 
                           String type) {}
    
    record OpenRouteGeometry(double[][] coordinates, String type) {}
    
    record OpenRouteProperties(double distance, 
                              double duration, 
                              OpenRouteSegment[] segments) {}
    
    record OpenRouteSegment(double distance, 
                           double duration, 
                           String instruction, 
                           double[][] way_points) {}
    
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