package com.example.routing_service.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "flags")
public class Flag {
    @Id
    private String id;
    
    private double latitude;
    
    private double longitude;
    
    private String reason;
    
    private LocalDateTime timestamp;
    
    private String reportedBy; // User ID who reported this flag
    
    public Flag(double latitude, double longitude, String reason, String reportedBy) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.reason = reason;
        this.reportedBy = reportedBy;
        this.timestamp = LocalDateTime.now();
    }
}
