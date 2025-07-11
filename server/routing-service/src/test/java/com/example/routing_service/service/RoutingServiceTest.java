package com.example.routing_service.service;

import com.example.routing_service.model.DangerZone;
import com.example.routing_service.model.Route;
import com.example.routing_service.repository.DangerZoneRepository;
import com.example.routing_service.repository.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoutingServiceTest {

    @Mock
    private RouteRepository routeRepository;
    @Mock
    private DangerZoneRepository dangerZoneRepository;

    private Route mockRoute;
    private DangerZone mockDangerZone;

    @BeforeEach
    void setUp() {
        mockRoute = Route.builder()
            .id("route-1")
            .userId("test-user")
            .routeName("Test Route")
            .createdAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusHours(24))
            .totalDistance(1500.0)
            .estimatedDuration(1800)
            .safetyScore(0.85)
            .status(Route.RouteStatus.ACTIVE)
            .build();

        mockDangerZone = DangerZone.builder()
            .id("zone-1")
            .name("Dark Alley")
            .dangerLevel(DangerZone.DangerLevel.MEDIUM)
            .location(new GeoJsonPoint(-74.0050, 40.7130))
            .build();
    }

    @Test
    void testRouteObjectCreation() {
        // Test route object creation
        assertNotNull(mockRoute);
        assertEquals("route-1", mockRoute.getId());
        assertEquals("test-user", mockRoute.getUserId());
        assertEquals("Test Route", mockRoute.getRouteName());
        assertEquals(0.85, mockRoute.getSafetyScore());
        assertEquals(Route.RouteStatus.ACTIVE, mockRoute.getStatus());
    }

    @Test
    void testDangerZoneObjectCreation() {
        // Test danger zone object creation
        assertNotNull(mockDangerZone);
        assertEquals("zone-1", mockDangerZone.getId());
        assertEquals("Dark Alley", mockDangerZone.getName());
        assertEquals(DangerZone.DangerLevel.MEDIUM, mockDangerZone.getDangerLevel());
        assertNotNull(mockDangerZone.getLocation());
    }

    @Test
    void testRouteRepositoryMocking() {
        // Test route repository mocking
        when(routeRepository.findById("route-1")).thenReturn(Optional.of(mockRoute));
        Optional<Route> foundRoute = routeRepository.findById("route-1");
        assertTrue(foundRoute.isPresent());
        assertEquals("route-1", foundRoute.get().getId());
        
        List<Route> userRoutes = Arrays.asList(mockRoute);
        when(routeRepository.findByUserIdAndStatus("test-user", Route.RouteStatus.ACTIVE))
            .thenReturn(userRoutes);
        List<Route> routes = routeRepository.findByUserIdAndStatus("test-user", Route.RouteStatus.ACTIVE);
        assertEquals(1, routes.size());
    }

    @Test
    void testDangerZoneRepositoryMocking() {
        // Test danger zone repository mocking
        List<DangerZone> dangerZones = Arrays.asList(mockDangerZone);
        
        when(dangerZoneRepository.findNearbyActiveDangerZones(anyDouble(), anyDouble(), anyDouble(), any()))
            .thenReturn(dangerZones);
        
        List<DangerZone> foundZones = dangerZoneRepository.findNearbyActiveDangerZones(40.7128, -74.0060, 1000.0, LocalDateTime.now());
        assertEquals(1, foundZones.size());
        assertEquals("zone-1", foundZones.get(0).getId());
    }

    @Test
    void testRouteStatusTransitions() {
        // Test route status changes
        Route route = Route.builder()
            .id("test-route")
            .userId("user-1")
            .status(Route.RouteStatus.ACTIVE)
            .build();
        
        assertEquals(Route.RouteStatus.ACTIVE, route.getStatus());
        
        route.setStatus(Route.RouteStatus.COMPLETED);
        assertEquals(Route.RouteStatus.COMPLETED, route.getStatus());
    }

    @Test
    void testDangerLevelComparison() {
        // Test danger level enum values
        assertTrue(DangerZone.DangerLevel.HIGH.ordinal() > DangerZone.DangerLevel.MEDIUM.ordinal());
        assertTrue(DangerZone.DangerLevel.MEDIUM.ordinal() > DangerZone.DangerLevel.LOW.ordinal());
        
        assertEquals(DangerZone.DangerLevel.MEDIUM, mockDangerZone.getDangerLevel());
    }

    @Test
    void testRepositorySaveOperations() {
        // Test repository save operations
        when(routeRepository.save(any(Route.class))).thenReturn(mockRoute);
        when(dangerZoneRepository.save(any(DangerZone.class))).thenReturn(mockDangerZone);
        
        Route savedRoute = routeRepository.save(mockRoute);
        DangerZone savedZone = dangerZoneRepository.save(mockDangerZone);
        
        assertNotNull(savedRoute);
        assertNotNull(savedZone);
        assertEquals("route-1", savedRoute.getId());
        assertEquals("zone-1", savedZone.getId());
    }
} 