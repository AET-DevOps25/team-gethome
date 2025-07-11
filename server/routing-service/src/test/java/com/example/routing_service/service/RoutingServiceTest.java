package com.example.routing_service.service;

import com.example.routing_service.client.OpenRouteServiceClient;
import com.example.routing_service.client.UserManagementClient;
import com.example.routing_service.dto.RouteRequest;
import com.example.routing_service.dto.RouteResponse;
import com.example.routing_service.model.DangerZone;
import com.example.routing_service.model.Route;
import com.example.routing_service.repository.DangerZoneRepository;
import com.example.routing_service.repository.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoutingServiceTest {

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private DangerZoneRepository dangerZoneRepository;

    @Mock
    private OpenRouteServiceClient openRouteServiceClient;

    @Mock
    private UserManagementClient userManagementClient;

    @Mock
    private SafetyAnalysisService safetyAnalysisService;

    @InjectMocks
    private RoutingService routingService;

    private RouteRequest routeRequest;
    private Route mockRoute;

    @BeforeEach
    void setUp() {
        // Set the @Value field manually for tests
        ReflectionTestUtils.setField(routingService, "openRouteApiKey", "test-openroute-api-key-for-testing");
        
        routeRequest = RouteRequest.builder()
            .startLocation(RouteRequest.Location.builder()
                .latitude(40.7128)
                .longitude(-74.0060)
                .address("New York, NY")
                .build())
            .endLocation(RouteRequest.Location.builder()
                .latitude(40.7589)
                .longitude(-73.9851)
                .address("Times Square, NY")
                .build())
            .safetyPreference(0.8)
            .routeName("Test Route")
            .userId("test-user")
            .build();

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
            .startLocation(Route.Location.builder()
                .latitude(40.7128)
                .longitude(-74.0060)
                .address("New York, NY")
                .build())
            .endLocation(Route.Location.builder()
                .latitude(40.7589)
                .longitude(-73.9851)
                .address("Times Square, NY")
                .build())
            .segments(Arrays.asList(
                Route.RouteSegment.builder()
                    .distance(1500.0)
                    .duration(1800)
                    .instructions("Walk straight")
                    .coordinates(Arrays.asList(
                        Route.Location.builder()
                            .latitude(40.7128)
                            .longitude(-74.0060)
                            .build(),
                        Route.Location.builder()
                            .latitude(40.7589)
                            .longitude(-73.9851)
                            .build()
                    ))
                    .build()
            ))
            .avoidedDangerZones(Arrays.asList("zone-1"))
            .build();
    }

    @Test
    void planSafeRoute_Success() {
        // Given
        String userId = "test-user";
        List<DangerZone> nearbyZones = Arrays.asList(
            DangerZone.builder()
                .id("zone-1")
                .name("Dark Alley")
                .dangerLevel(DangerZone.DangerLevel.MEDIUM)
                .location(new GeoJsonPoint(-74.0050, 40.7130))
                .build()
        );

        when(dangerZoneRepository.findNearbyActiveDangerZones(anyDouble(), anyDouble(), anyDouble(), any()))
            .thenReturn(nearbyZones);
        when(openRouteServiceClient.getWalkingRoute(any()))
            .thenReturn(createMockOpenRouteResponse());
        when(safetyAnalysisService.calculateSafetyScore(any(), anyList()))
            .thenReturn(0.85);
        when(routeRepository.save(any(Route.class)))
            .thenReturn(mockRoute);

        // When
        RouteResponse response = routingService.planSafeRoute(routeRequest, userId);

        // Then
        assertNotNull(response);
        assertEquals("route-1", response.getRouteId());
        assertEquals("Test Route", response.getRouteName());
        assertEquals(0.85, response.getSafetyScore());
        verify(routeRepository).save(any(Route.class));
    }

    @Test
    void getUserRoutes_Success() {
        // Given
        String userId = "test-user";
        List<Route> routes = Arrays.asList(mockRoute);
        when(routeRepository.findByUserIdAndStatus(userId, Route.RouteStatus.ACTIVE))
            .thenReturn(routes);

        // When
        List<RouteResponse> responses = routingService.getUserRoutes(userId);

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("route-1", responses.get(0).getRouteId());
    }

    @Test
    void getRoute_Success() {
        // Given
        String routeId = "route-1";
        String userId = "test-user";
        when(routeRepository.findById(routeId))
            .thenReturn(java.util.Optional.of(mockRoute));

        // When
        RouteResponse response = routingService.getRoute(routeId, userId);

        // Then
        assertNotNull(response);
        assertEquals("route-1", response.getRouteId());
    }

    @Test
    void getRoute_AccessDenied() {
        // Given
        String routeId = "route-1";
        String userId = "different-user";
        when(routeRepository.findById(routeId))
            .thenReturn(java.util.Optional.of(mockRoute));

        // When & Then
        assertThrows(RuntimeException.class, () -> 
            routingService.getRoute(routeId, userId));
    }

    @Test
    void completeRoute_Success() {
        // Given
        String routeId = "route-1";
        String userId = "test-user";
        when(routeRepository.findById(routeId))
            .thenReturn(java.util.Optional.of(mockRoute));
        when(routeRepository.save(any(Route.class)))
            .thenReturn(mockRoute);

        // When
        routingService.completeRoute(routeId, userId);

        // Then
        verify(routeRepository).save(any(Route.class));
    }

    private OpenRouteServiceClient.OpenRouteResponse createMockOpenRouteResponse() {
        // Create a valid polyline-encoded geometry string that represents a simple route
        // This encodes two points: start and end coordinates
        String validPolylineGeometry = "u{~vFd~lbMnI|\\";
        
        OpenRouteServiceClient.OpenRouteStep step = 
            new OpenRouteServiceClient.OpenRouteStep(
                1500.0, 
                1800.0, 
                "Walk straight down the street", 
                "Test Street", 
                new int[]{0, 1}
            );
        
        OpenRouteServiceClient.OpenRouteSegment segment = 
            new OpenRouteServiceClient.OpenRouteSegment(
                1500.0, 
                1800.0, 
                new OpenRouteServiceClient.OpenRouteStep[]{step}
            );
        
        OpenRouteServiceClient.OpenRouteSummary summary = 
            new OpenRouteServiceClient.OpenRouteSummary(1500.0, 1800.0);
        
        OpenRouteServiceClient.OpenRouteRoute route = 
            new OpenRouteServiceClient.OpenRouteRoute(
                summary, 
                new OpenRouteServiceClient.OpenRouteSegment[]{segment}, 
                validPolylineGeometry, 
                new int[]{0, 1}
            );
        
        OpenRouteServiceClient.OpenRouteQuery query = 
            new OpenRouteServiceClient.OpenRouteQuery(
                new double[][]{{-74.0060, 40.7128}, {-73.9851, 40.7589}}, 
                "foot-walking", 
                "recommended", 
                "m", 
                "en"
            );
        
        OpenRouteServiceClient.OpenRouteMetadata metadata = 
            new OpenRouteServiceClient.OpenRouteMetadata(
                "ORS", 
                "routing", 
                System.currentTimeMillis(), 
                query
            );
        
        return new OpenRouteServiceClient.OpenRouteResponse(
            new OpenRouteServiceClient.OpenRouteRoute[]{route}, 
            metadata
        );
    }
} 