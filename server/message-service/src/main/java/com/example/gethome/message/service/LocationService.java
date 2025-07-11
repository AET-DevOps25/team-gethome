package com.example.gethome.message.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Slf4j
public class LocationService {

    private final RestTemplate restTemplate = new RestTemplate();

    public String reverseGeocode(double latitude, double longitude) {
        try {
            // Using OpenStreetMap Nominatim API (free, no API key required)
            String url = String.format("https://nominatim.openstreetmap.org/reverse?format=json&lat=%.6f&lon=%.6f&zoom=18&addressdetails=1", 
                                     latitude, longitude);
            
            log.debug("Reverse geocoding request: {}", url);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && response.containsKey("display_name")) {
                String displayName = (String) response.get("display_name");
                log.debug("Reverse geocoding result: {}", displayName);
                return displayName;
            }
            
        } catch (Exception e) {
            log.warn("Failed to reverse geocode coordinates ({}, {}): {}", latitude, longitude, e.getMessage());
        }
        
        // Fallback to coordinates if reverse geocoding fails
        return String.format("Coordinates: %.6f, %.6f", latitude, longitude);
    }

    public String getReadableLocation(double latitude, double longitude, String fallbackLocation) {
        // If we have a meaningful fallback location, use it
        if (fallbackLocation != null && !fallbackLocation.trim().isEmpty() && 
            !"Unknown".equals(fallbackLocation) && !"Current location".equals(fallbackLocation)) {
            return fallbackLocation;
        }
        
        // Try to get a human-readable address
        return reverseGeocode(latitude, longitude);
    }
} 