package com.example.routing_service.controller;

import com.example.routing_service.dto.RouteRequest;
import com.example.routing_service.dto.RouteResponse;
import com.example.routing_service.service.RoutingService;
import com.example.routing_service.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
@Slf4j
public class RoutingController {

    private final RoutingService routingService;
    private final JwtService jwtService;

    @PostMapping("/plan")
    public ResponseEntity<RouteResponse> planSafeRoute(@RequestBody RouteRequest request, @RequestHeader("Authorization") String authHeader) {
        String userId = jwtService.extractUserId(authHeader.substring(7));
        try {
            RouteResponse response = routingService.planSafeRoute(request, userId);
            try {
                ObjectMapper mapper = new ObjectMapper();
                log.info("RouteResponse JSON: {}", mapper.writeValueAsString(response));
            } catch (Exception e) {
                log.error("Failed to serialize RouteResponse", e);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to plan safe route for user: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<RouteResponse>> getUserRoutes(Authentication authentication) {
        String userId = authentication.getName();
        log.info("Getting routes for user: {}", userId);
        
        List<RouteResponse> routes = routingService.getUserRoutes(userId);
        return ResponseEntity.ok(routes);
    }

    @GetMapping("/{routeId}")
    public ResponseEntity<RouteResponse> getRoute(@PathVariable String routeId,
                                                 Authentication authentication) {
        String userId = authentication.getName();
        log.info("Getting route {} for user: {}", routeId, userId);
        
        RouteResponse route = routingService.getRoute(routeId, userId);
        return ResponseEntity.ok(route);
    }

    @PostMapping("/{routeId}/complete")
    public ResponseEntity<Void> completeRoute(@PathVariable String routeId,
                                             Authentication authentication) {
        String userId = authentication.getName();
        log.info("Completing route {} for user: {}", routeId, userId);
        
        routingService.completeRoute(routeId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Routing service is healthy");
    }
} 