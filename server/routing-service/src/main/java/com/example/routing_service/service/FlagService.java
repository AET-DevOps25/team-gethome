package com.example.routing_service.service;

import com.example.routing_service.dto.FlagLocationRequest;
import com.example.routing_service.dto.FlagResponse;
import com.example.routing_service.model.Flag;
import com.example.routing_service.repository.FlagRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlagService {
    
    private final FlagRepository flagRepository;
    private static final Logger logger = LoggerFactory.getLogger(FlagService.class);
    
    public FlagResponse flagLocation(FlagLocationRequest request, String userId) {
        logger.info("Creating flag for location lat: {}, lon: {}, reason: {}, userId: {}", 
                   request.getLatitude(), request.getLongitude(), request.getReason(), userId);
        
        Flag flag = new Flag(
            request.getLatitude(),
            request.getLongitude(),
            request.getReason(),
            userId
        );
        
        Flag savedFlag = flagRepository.save(flag);
        logger.info("Flag created successfully with id: {}", savedFlag.getId());
        
        return FlagResponse.fromFlag(savedFlag);
    }
    
    public List<FlagResponse> getFlagsWithinDistance(double latitude, double longitude, double distanceMeters) {
        logger.info("Getting flags within {} meters of lat: {}, lon: {}", distanceMeters, latitude, longitude);
        
        // Convert distance from meters to approximate degrees
        // 1 degree of latitude ≈ 111,111 meters
        // 1 degree of longitude ≈ 111,111 * cos(latitude) meters
        double latDelta = distanceMeters / 111111.0;
        double lonDelta = distanceMeters / (111111.0 * Math.cos(Math.toRadians(latitude)));
        
        double minLat = latitude - latDelta;
        double maxLat = latitude + latDelta;
        double minLon = longitude - lonDelta;
        double maxLon = longitude + lonDelta;
        
        List<Flag> flags = flagRepository.findFlagsWithinBounds(latitude, longitude, minLat, maxLat, minLon, maxLon);
        
        // Filter by exact distance using Haversine formula
        List<Flag> filteredFlags = flags.stream()
            .filter(flag -> calculateDistance(latitude, longitude, flag.getLatitude(), flag.getLongitude()) <= distanceMeters)
            .collect(Collectors.toList());
        
        logger.info("Found {} flags within distance", filteredFlags.size());
        
        return filteredFlags.stream()
            .map(FlagResponse::fromFlag)
            .collect(Collectors.toList());
    }
    
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Haversine formula to calculate distance between two points
        final int EARTH_RADIUS = 6371000; // Earth's radius in meters
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS * c;
    }
}
