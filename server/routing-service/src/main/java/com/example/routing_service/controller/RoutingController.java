package com.example.routing_service.controller;

import com.example.routing_service.dto.*;
import com.example.routing_service.security.JwtService;
import com.example.routing_service.service.FlagService;
import com.example.routing_service.service.RouteService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RoutingController {
    
    private final FlagService flagService;
    private final RouteService routeService;
    private final JwtService jwtService;
    private static final Logger logger = LoggerFactory.getLogger(RoutingController.class);
    
    @PostMapping("/flags")
    public ResponseEntity<FlagResponse> flagLocation(
            @Valid @RequestBody FlagLocationRequest request,
            HttpServletRequest httpRequest) {
        
        // Extract token and get user ID
        String token = httpRequest.getHeader("Authorization").substring(7);
        String authenticatedUserId = jwtService.extractUserId(token);
        
        logger.info("User {} flagging location at lat: {}, lon: {}", 
                   authenticatedUserId, request.getLatitude(), request.getLongitude());
        
        try {
            FlagResponse response = flagService.flagLocation(request, authenticatedUserId);
            logger.info("Location flagged successfully with id: {}", response.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error flagging location: {}", e.getMessage());
            throw e;
        }
    }
    
    @GetMapping("/flags")
    public ResponseEntity<List<FlagResponse>> getFlags(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam @Positive(message = "Distance must be positive") double distance,
            HttpServletRequest httpRequest) {
        
        // Extract token for authentication (user must be authenticated to see flags)
        String token = httpRequest.getHeader("Authorization").substring(7);
        String authenticatedUserId = jwtService.extractUserId(token);
        
        logger.info("User {} requesting flags within {} meters of lat: {}, lon: {}", 
                   authenticatedUserId, distance, lat, lon);
        
        try {
            List<FlagResponse> flags = flagService.getFlagsWithinDistance(lat, lon, distance);
            logger.info("Retrieved {} flags within specified distance", flags.size());
            return ResponseEntity.ok(flags);
        } catch (Exception e) {
            logger.error("Error retrieving flags: {}", e.getMessage());
            throw e;
        }
    }
    
    @PostMapping("/plan")
    public ResponseEntity<RouteResponse> planRoute(
            @Valid @RequestBody RoutePlanRequest request,
            HttpServletRequest httpRequest) {
        
        // Extract token and get user ID
        String token = httpRequest.getHeader("Authorization").substring(7);
        String authenticatedUserId = jwtService.extractUserId(token);
        
        logger.info("User {} planning route from ({}, {}) to ({}, {})", 
                   authenticatedUserId, request.getStartLatitude(), request.getStartLongitude(),
                   request.getEndLatitude(), request.getEndLongitude());
        
        try {
            RouteResponse response = routeService.planRoute(request, authenticatedUserId);
            logger.info("Route planned successfully with id: {}", response.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error planning route: {}", e.getMessage());
            throw e;
        }
    }
    
    @PutMapping("/replan")
    public ResponseEntity<RouteResponse> replanRoute(
            @Valid @RequestBody RouteReplanRequest request,
            HttpServletRequest httpRequest) {
        
        // Extract token and get user ID
        String token = httpRequest.getHeader("Authorization").substring(7);
        String authenticatedUserId = jwtService.extractUserId(token);
        
        logger.info("User {} replanning route for journey: {}", 
                   authenticatedUserId, request.getJourneyId());
        
        try {
            RouteResponse response = routeService.replanRoute(request, authenticatedUserId);
            logger.info("Route replanned successfully for journey: {}", request.getJourneyId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error replanning route: {}", e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            if (e.getMessage().contains("can only")) {
                throw new AccessDeniedException(e.getMessage());
            }
            throw e;
        }
    }
    
    @GetMapping("/{journeyId}")
    public ResponseEntity<RouteResponse> getRoute(
            @PathVariable String journeyId,
            HttpServletRequest httpRequest) {
        
        // Extract token and get user ID
        String token = httpRequest.getHeader("Authorization").substring(7);
        String authenticatedUserId = jwtService.extractUserId(token);
        
        logger.info("User {} requesting route for journey: {}", authenticatedUserId, journeyId);
        
        try {
            RouteResponse response = routeService.getRoute(journeyId, authenticatedUserId);
            logger.info("Route retrieved successfully for journey: {}", journeyId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving route: {}", e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            if (e.getMessage().contains("can only")) {
                throw new AccessDeniedException(e.getMessage());
            }
            throw e;
        }
    }
}
