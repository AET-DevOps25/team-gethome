package com.example.routing_service.service;

import com.example.routing_service.dto.DangerZoneRequest;
import com.example.routing_service.model.DangerZone;
import com.example.routing_service.repository.DangerZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DangerZoneService {

    private final DangerZoneRepository dangerZoneRepository;

    public DangerZone reportDangerZone(DangerZoneRequest request, String userId) {
        log.info("User {} reporting danger zone: {}", userId, request.getName());

        // Check if a similar danger zone already exists nearby
        Optional<DangerZone> existingZone = findNearbyExistingZone(
            request.getLocation().getLatitude(),
            request.getLocation().getLongitude(),
            request.getName()
        );

        if (existingZone.isPresent()) {
            // Update existing zone
            DangerZone existing = existingZone.get();
            existing.setReportCount(existing.getReportCount() + 1);
            
            if (!existing.getReportedByUsers().contains(userId)) {
                existing.getReportedByUsers().add(userId);
            }
            
            // Update danger level if new report has higher level
            if (request.getDangerLevel().ordinal() > existing.getDangerLevel().ordinal()) {
                existing.setDangerLevel(request.getDangerLevel());
            }
            
            // Extend expiration time
            existing.setExpiresAt(LocalDateTime.now().plusDays(30));
            
            log.info("Updated existing danger zone: {}", existing.getId());
            return dangerZoneRepository.save(existing);
        } else {
            // Create new danger zone
            DangerZone newZone = DangerZone.builder()
                .name(request.getName())
                .description(request.getDescription())
                .dangerLevel(request.getDangerLevel())
                .reportedBy(userId)
                .reportedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(30))
                .tags(request.getTags())
                .location(DangerZone.Location.builder()
                    .type("Point")
                    .coordinates(new double[]{
                        request.getLocation().getLongitude(),
                        request.getLocation().getLatitude()
                    })
                    .build())
                .reportCount(1)
                .reportedByUsers(List.of(userId))
                .build();

            log.info("Created new danger zone: {}", newZone.getId());
            return dangerZoneRepository.save(newZone);
        }
    }

    private Optional<DangerZone> findNearbyExistingZone(double latitude, double longitude, String name) {
        // Look for existing zones within 50 meters with similar name
        List<DangerZone> nearbyZones = dangerZoneRepository.findNearbyActiveDangerZones(
            longitude, latitude, 50, LocalDateTime.now()
        );

        return nearbyZones.stream()
            .filter(zone -> zone.getName().toLowerCase().contains(name.toLowerCase()) ||
                           name.toLowerCase().contains(zone.getName().toLowerCase()))
            .findFirst();
    }

    public List<DangerZone> getNearbyDangerZones(double latitude, double longitude, double radius) {
        log.info("Finding danger zones near ({}, {}) within {} meters", latitude, longitude, radius);
        
        return dangerZoneRepository.findNearbyActiveDangerZones(
            longitude, latitude, radius, LocalDateTime.now()
        );
    }

    public List<DangerZone> getDangerZonesByLevel(DangerZone.DangerLevel level) {
        return dangerZoneRepository.findByDangerLevel(level);
    }

    public List<DangerZone> getDangerZonesByTag(String tag) {
        return dangerZoneRepository.findByTagsContaining(tag);
    }

    public List<DangerZone> getUserReportedZones(String userId) {
        return dangerZoneRepository.findByReportedByUsersContaining(userId);
    }

    public void cleanupExpiredZones() {
        log.info("Cleaning up expired danger zones");
        
        List<DangerZone> expiredZones = dangerZoneRepository.findExpiredDangerZones(LocalDateTime.now());
        
        if (!expiredZones.isEmpty()) {
            dangerZoneRepository.deleteAll(expiredZones);
            log.info("Deleted {} expired danger zones", expiredZones.size());
        }
    }

    public DangerZone getDangerZone(String zoneId) {
        return dangerZoneRepository.findById(zoneId)
            .orElseThrow(() -> new RuntimeException("Danger zone not found"));
    }

    public void deleteDangerZone(String zoneId, String userId) {
        DangerZone zone = getDangerZone(zoneId);
        
        if (!zone.getReportedBy().equals(userId)) {
            throw new RuntimeException("Only the original reporter can delete a danger zone");
        }
        
        dangerZoneRepository.delete(zone);
        log.info("User {} deleted danger zone: {}", userId, zoneId);
    }

    public DangerZone updateDangerZone(String zoneId, DangerZoneRequest request, String userId) {
        DangerZone zone = getDangerZone(zoneId);
        
        if (!zone.getReportedBy().equals(userId)) {
            throw new RuntimeException("Only the original reporter can update a danger zone");
        }
        
        zone.setName(request.getName());
        zone.setDescription(request.getDescription());
        zone.setDangerLevel(request.getDangerLevel());
        zone.setTags(request.getTags());
        zone.setLocation(DangerZone.Location.builder()
            .type("Point")
            .coordinates(new double[]{
                request.getLocation().getLongitude(),
                request.getLocation().getLatitude()
            })
            .build());
        
        log.info("User {} updated danger zone: {}", userId, zoneId);
        return dangerZoneRepository.save(zone);
    }
} 