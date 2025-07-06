package com.example.routing_service.controller;

import com.example.routing_service.dto.DangerZoneRequest;
import com.example.routing_service.model.DangerZone;
import com.example.routing_service.service.DangerZoneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/danger-zones")
@RequiredArgsConstructor
@Slf4j
public class DangerZoneController {

    private final DangerZoneService dangerZoneService;

    @PostMapping("/report")
    public ResponseEntity<DangerZone> reportDangerZone(@Valid @RequestBody DangerZoneRequest request,
                                                       Authentication authentication) {
        String userId = authentication.getName();
        log.info("Danger zone report from user: {}", userId);
        
        DangerZone dangerZone = dangerZoneService.reportDangerZone(request, userId);
        return ResponseEntity.ok(dangerZone);
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<DangerZone>> getNearbyDangerZones(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "1000") double radius) {
        log.info("Getting danger zones near ({}, {}) within {} meters", latitude, longitude, radius);
        
        List<DangerZone> dangerZones = dangerZoneService.getNearbyDangerZones(latitude, longitude, radius);
        return ResponseEntity.ok(dangerZones);
    }

    @GetMapping("/level/{level}")
    public ResponseEntity<List<DangerZone>> getDangerZonesByLevel(@PathVariable DangerZone.DangerLevel level) {
        log.info("Getting danger zones with level: {}", level);
        
        List<DangerZone> dangerZones = dangerZoneService.getDangerZonesByLevel(level);
        return ResponseEntity.ok(dangerZones);
    }

    @GetMapping("/tag/{tag}")
    public ResponseEntity<List<DangerZone>> getDangerZonesByTag(@PathVariable String tag) {
        log.info("Getting danger zones with tag: {}", tag);
        
        List<DangerZone> dangerZones = dangerZoneService.getDangerZonesByTag(tag);
        return ResponseEntity.ok(dangerZones);
    }

    @GetMapping("/my-reports")
    public ResponseEntity<List<DangerZone>> getMyReportedZones(Authentication authentication) {
        String userId = authentication.getName();
        log.info("Getting danger zones reported by user: {}", userId);
        
        List<DangerZone> dangerZones = dangerZoneService.getUserReportedZones(userId);
        return ResponseEntity.ok(dangerZones);
    }

    @GetMapping("/{zoneId}")
    public ResponseEntity<DangerZone> getDangerZone(@PathVariable String zoneId) {
        log.info("Getting danger zone: {}", zoneId);
        
        DangerZone dangerZone = dangerZoneService.getDangerZone(zoneId);
        return ResponseEntity.ok(dangerZone);
    }

    @PutMapping("/{zoneId}")
    public ResponseEntity<DangerZone> updateDangerZone(@PathVariable String zoneId,
                                                       @Valid @RequestBody DangerZoneRequest request,
                                                       Authentication authentication) {
        String userId = authentication.getName();
        log.info("Updating danger zone {} by user: {}", zoneId, userId);
        
        DangerZone dangerZone = dangerZoneService.updateDangerZone(zoneId, request, userId);
        return ResponseEntity.ok(dangerZone);
    }

    @DeleteMapping("/{zoneId}")
    public ResponseEntity<Void> deleteDangerZone(@PathVariable String zoneId,
                                                Authentication authentication) {
        String userId = authentication.getName();
        log.info("Deleting danger zone {} by user: {}", zoneId, userId);
        
        dangerZoneService.deleteDangerZone(zoneId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cleanup")
    public ResponseEntity<Void> cleanupExpiredZones() {
        log.info("Cleaning up expired danger zones");
        
        dangerZoneService.cleanupExpiredZones();
        return ResponseEntity.ok().build();
    }
} 