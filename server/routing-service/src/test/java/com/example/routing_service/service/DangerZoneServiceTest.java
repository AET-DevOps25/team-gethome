package com.example.routing_service.service;

import com.example.routing_service.dto.DangerZoneRequest;
import com.example.routing_service.model.DangerZone;
import com.example.routing_service.repository.DangerZoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DangerZoneServiceTest {

    @Mock
    private DangerZoneRepository dangerZoneRepository;

    @InjectMocks
    private DangerZoneService dangerZoneService;

    private DangerZoneRequest request;
    private DangerZone mockDangerZone;

    @BeforeEach
    void setUp() {
        request = DangerZoneRequest.builder()
            .name("Dark Alley")
            .description("Poorly lit alley with suspicious activity")
            .dangerLevel(DangerZoneRequest.DangerLevel.MEDIUM)
            .location(DangerZoneRequest.Location.builder()
                .latitude(40.7128)
                .longitude(-74.0060)
                .build())
            .tags(Arrays.asList("alley", "poor_lighting"))
            .build();

        mockDangerZone = DangerZone.builder()
            .id("zone-1")
            .name("Dark Alley")
            .description("Poorly lit alley with suspicious activity")
            .dangerLevel(DangerZone.DangerLevel.MEDIUM)
            .reportedBy("test-user")
            .reportedAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusDays(30))
            .tags(Arrays.asList("alley", "poor_lighting"))
            .location(DangerZone.Location.builder()
                .type("Point")
                .coordinates(new double[]{-74.0060, 40.7128})
                .build())
            .reportCount(1)
            .reportedByUsers(Arrays.asList("test-user"))
            .build();
    }

    @Test
    void reportDangerZone_NewZone_Success() {
        // Given
        String userId = "test-user";
        when(dangerZoneRepository.findNearbyActiveDangerZones(anyDouble(), anyDouble(), anyDouble(), any()))
            .thenReturn(Arrays.asList());
        when(dangerZoneRepository.save(any(DangerZone.class)))
            .thenReturn(mockDangerZone);

        // When
        DangerZone result = dangerZoneService.reportDangerZone(request, userId);

        // Then
        assertNotNull(result);
        assertEquals("zone-1", result.getId());
        assertEquals("Dark Alley", result.getName());
        assertEquals(1, result.getReportCount());
        verify(dangerZoneRepository).save(any(DangerZone.class));
    }

    @Test
    void getNearbyDangerZones_Success() {
        // Given
        double latitude = 40.7128;
        double longitude = -74.0060;
        double radius = 1000;
        List<DangerZone> expectedZones = Arrays.asList(mockDangerZone);

        when(dangerZoneRepository.findNearbyActiveDangerZones(longitude, latitude, radius, any()))
            .thenReturn(expectedZones);

        // When
        List<DangerZone> result = dangerZoneService.getNearbyDangerZones(latitude, longitude, radius);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("zone-1", result.get(0).getId());
    }

    @Test
    void getDangerZone_Success() {
        // Given
        String zoneId = "zone-1";
        when(dangerZoneRepository.findById(zoneId))
            .thenReturn(Optional.of(mockDangerZone));

        // When
        DangerZone result = dangerZoneService.getDangerZone(zoneId);

        // Then
        assertNotNull(result);
        assertEquals("zone-1", result.getId());
    }

    @Test
    void getDangerZone_NotFound() {
        // Given
        String zoneId = "non-existent";
        when(dangerZoneRepository.findById(zoneId))
            .thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> 
            dangerZoneService.getDangerZone(zoneId));
    }
} 