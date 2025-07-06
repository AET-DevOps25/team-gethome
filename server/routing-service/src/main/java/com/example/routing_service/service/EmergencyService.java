package com.example.routing_service.service;

import com.example.routing_service.client.MessageServiceClient;
import com.example.routing_service.client.UserManagementClient;
import com.example.routing_service.dto.EmergencyNotificationRequest;
import com.example.routing_service.dto.EmergencyNotificationResponse;
import com.example.routing_service.model.DangerZone;
import com.example.routing_service.model.Route;
import com.example.routing_service.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmergencyService {

    private final RouteRepository routeRepository;
    private final DangerZoneService dangerZoneService;
    private final MessageServiceClient messageServiceClient;
    private final UserManagementClient userManagementClient;

    public EmergencyNotificationResponse triggerEmergency(String userId, 
                                                         String reason, 
                                                         double latitude, 
                                                         double longitude, 
                                                         String location,
                                                         String audioSnippet,
                                                         String authToken) {
        log.warn("Emergency triggered for user: {} at location: {},{}", userId, latitude, longitude);
        
        try {
            // Get user's emergency contacts
            UserManagementClient.EmergencyContactsResponse contactsResponse = 
                userManagementClient.getEmergencyContacts(userId, "Bearer " + authToken);
            
            if (contactsResponse.emergencyContacts().isEmpty()) {
                log.warn("No emergency contacts found for user: {}", userId);
                return EmergencyNotificationResponse.builder()
                    .id("no-contacts")
                    .userId(userId)
                    .status("NO_CONTACTS")
                    .message("No emergency contacts found")
                    .build();
            }
            
            // Create emergency notification request
            EmergencyNotificationRequest request = EmergencyNotificationRequest.builder()
                .userId(userId)
                .message("Emergency triggered: " + reason)
                .latitude(latitude)
                .longitude(longitude)
                .location(location)
                .audioSnippet(audioSnippet)
                .emergencyType("MANUAL")
                .reason(reason)
                .emergencyContactIds(contactsResponse.emergencyContacts().stream()
                    .map(UserManagementClient.EmergencyContact::id)
                    .toList())
                .build();
            
            // Map DTO to client record
            MessageServiceClient.EmergencyNotificationRequest clientRequest =
                new MessageServiceClient.EmergencyNotificationRequest(
                    request.getUserId(),
                    request.getMessage(),
                    request.getLatitude(),
                    request.getLongitude(),
                    request.getAudioSnippet(),
                    request.getEmergencyContactIds()
                );
            // Send emergency notification via message service
            MessageServiceClient.EmergencyNotificationResponse clientResponse = messageServiceClient.sendEmergencyNotification(clientRequest, "Bearer " + authToken);
            EmergencyNotificationResponse response = EmergencyNotificationResponse.builder()
                .id(clientResponse.id())
                .status(clientResponse.status())
                .message(clientResponse.message())
                .userId(userId)
                .triggeredAt(java.time.LocalDateTime.now())
                .build();
            
            log.info("Emergency notification sent successfully for user: {} with status: {}", userId, response.getStatus());
            return response;
            
        } catch (Exception e) {
            log.error("Failed to trigger emergency for user: {}", userId, e);
            throw new RuntimeException("Emergency notification failed", e);
        }
    }

    public EmergencyNotificationResponse triggerAudioEmergency(String userId, 
                                                              double latitude, 
                                                              double longitude, 
                                                              String location,
                                                              String audioSnippet,
                                                              String authToken) {
        log.warn("Audio-based emergency triggered for user: {} at location: {},{}", userId, latitude, longitude);
        
        return triggerEmergency(userId, "Audio-based emergency detection", latitude, longitude, location, audioSnippet, authToken);
    }

    private String buildEmergencyMessage(String userId, double latitude, double longitude, 
                                       String reason, List<DangerZone> nearbyZones) {
        StringBuilder message = new StringBuilder();
        message.append("EMERGENCY ALERT: User ").append(userId).append(" has triggered an emergency.\n");
        message.append("Location: ").append(latitude).append(", ").append(longitude).append("\n");
        message.append("Reason: ").append(reason).append("\n");
        message.append("Time: ").append(LocalDateTime.now()).append("\n");
        
        if (!nearbyZones.isEmpty()) {
            message.append("Nearby danger zones:\n");
            for (DangerZone zone : nearbyZones) {
                message.append("- ").append(zone.getName())
                       .append(" (").append(zone.getDangerLevel()).append(")\n");
            }
        }
        
        return message.toString();
    }

    private void markRoutesAsAffected(String userId, double latitude, double longitude) {
        // Find active routes for the user and mark them as potentially affected
        List<Route> activeRoutes = routeRepository.findByUserIdAndStatus(userId, Route.RouteStatus.ACTIVE);
        
        for (Route route : activeRoutes) {
            // Check if the emergency location is near the route
            if (isLocationNearRoute(latitude, longitude, route)) {
                // Could add a field to mark routes as affected by emergency
                log.info("Marking route {} as potentially affected by emergency", route.getId());
            }
        }
    }

    private boolean isLocationNearRoute(double latitude, double longitude, Route route) {
        // Simple distance check - if emergency is within 1km of any route segment
        for (Route.RouteSegment segment : route.getSegments()) {
            if (segment.getCoordinates() != null) {
                for (Double[] coordinate : segment.getCoordinates()) {
                    double distance = calculateDistance(latitude, longitude, coordinate[1], coordinate[0]);
                    if (distance <= 1000) { // 1km
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lng2 - lng1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return 6371000 * c; // Earth radius in meters
    }

    private String getAuthToken() {
        // In a real implementation, this would get the service-to-service auth token
        // For now, return a placeholder
        return "service-token";
    }

    public void handleAudioEmergency(String userId, double latitude, double longitude, 
                                   String audioSnippet) {
        log.warn("Audio-based emergency detected for user: {} at location ({}, {})", 
                userId, latitude, longitude);
        
        handleEmergency(userId, latitude, longitude, audioSnippet, "Audio-based emergency detection");
    }

    public void handleManualEmergency(String userId, double latitude, double longitude) {
        log.warn("Manual emergency button pressed for user: {} at location ({}, {})", 
                userId, latitude, longitude);
        
        handleEmergency(userId, latitude, longitude, null, "Manual emergency button pressed");
    }

    private void handleEmergency(String userId, double latitude, double longitude, String audioSnippet, String reason) {
        // Use a placeholder location and auth token for now
        String location = "Unknown";
        String authToken = getAuthToken();
        triggerEmergency(userId, reason, latitude, longitude, location, audioSnippet, authToken);
    }
} 