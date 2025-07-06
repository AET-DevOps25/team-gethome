package com.example.routing_service.service;

import com.example.routing_service.model.DangerZone;
import com.example.routing_service.model.Route;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class SafetyAnalysisService {

    private static final double EARTH_RADIUS = 6371000; // Earth's radius in meters
    private static final double DANGER_ZONE_RADIUS = 100; // Consider danger zones within 100m of route

    public double calculateSafetyScore(Route route, List<DangerZone> nearbyDangerZones) {
        if (nearbyDangerZones.isEmpty()) {
            return 1.0; // Perfect safety score if no danger zones
        }

        double totalRisk = 0.0;
        int dangerZoneCount = 0;

        // Analyze each segment of the route
        for (Route.RouteSegment segment : route.getSegments()) {
            double segmentRisk = calculateSegmentRisk(segment, nearbyDangerZones);
            totalRisk += segmentRisk;
            dangerZoneCount++;
        }

        // Calculate average risk and convert to safety score
        double averageRisk = dangerZoneCount > 0 ? totalRisk / dangerZoneCount : 0.0;
        double safetyScore = Math.max(0.0, 1.0 - averageRisk);

        log.info("Calculated safety score: {} for route with {} danger zones", safetyScore, nearbyDangerZones.size());
        return safetyScore;
    }

    private double calculateSegmentRisk(Route.RouteSegment segment, List<DangerZone> dangerZones) {
        double segmentRisk = 0.0;

        for (DangerZone dangerZone : dangerZones) {
            double distance = calculateDistanceToSegment(segment, dangerZone);
            
            if (distance <= DANGER_ZONE_RADIUS) {
                double riskFactor = calculateRiskFactor(dangerZone, distance);
                segmentRisk += riskFactor;
            }
        }

        return Math.min(1.0, segmentRisk); // Cap risk at 1.0
    }

    private double calculateDistanceToSegment(Route.RouteSegment segment, DangerZone dangerZone) {
        double minDistance = Double.MAX_VALUE;

        // Calculate distance from danger zone to each coordinate in the segment
        if (segment.getCoordinates() != null) {
            for (Double[] coordinate : segment.getCoordinates()) {
                double distance = calculateDistance(
                    coordinate[1], coordinate[0], // lat, lng
                    dangerZone.getLocation().getCoordinates()[1], 
                    dangerZone.getLocation().getCoordinates()[0]
                );
                minDistance = Math.min(minDistance, distance);
            }
        }

        return minDistance;
    }

    private double calculateRiskFactor(DangerZone dangerZone, double distance) {
        // Base risk based on danger level
        double baseRisk = switch (dangerZone.getDangerLevel()) {
            case LOW -> 0.1;
            case MEDIUM -> 0.3;
            case HIGH -> 0.6;
            case CRITICAL -> 0.9;
        };

        // Distance factor (closer = higher risk)
        double distanceFactor = 1.0 - (distance / DANGER_ZONE_RADIUS);
        distanceFactor = Math.max(0.0, distanceFactor);

        // Report count factor (more reports = higher risk)
        double reportFactor = Math.min(1.0, dangerZone.getReportCount() / 10.0);

        return baseRisk * distanceFactor * (1.0 + reportFactor * 0.5);
    }

    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lng2 - lng1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS * c;
    }

    public boolean isRouteSafe(Route route, double safetyThreshold) {
        return route.getSafetyScore() >= safetyThreshold;
    }

    public List<DangerZone> getHighRiskZones(List<DangerZone> dangerZones) {
        return dangerZones.stream()
            .filter(zone -> zone.getDangerLevel() == DangerZone.DangerLevel.HIGH || 
                           zone.getDangerLevel() == DangerZone.DangerLevel.CRITICAL)
            .toList();
    }
} 