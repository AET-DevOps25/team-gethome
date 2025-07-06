package com.example.routing_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "danger_zones")
public class DangerZone {
    @Id
    private String id;
    
    private String name;
    private String description;
    private DangerLevel dangerLevel;
    private String reportedBy;
    private LocalDateTime reportedAt;
    private LocalDateTime expiresAt;
    private List<String> tags; // e.g., ["park", "alley", "poor_lighting"]
    
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private Location location;
    
    private int reportCount;
    private List<String> reportedByUsers;
    
    public enum DangerLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Location {
        @Builder.Default
        private String type = "Point";
        private double[] coordinates; // [longitude, latitude]
    }
} 