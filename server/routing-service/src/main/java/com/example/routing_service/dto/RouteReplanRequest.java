package com.example.routing_service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteReplanRequest {
    
    @NotBlank(message = "Journey ID is required")
    private String journeyId;
    
    @Positive(message = "Avoidance radius must be positive")
    private Double avoidanceRadiusMeters = 500.0; // Default 500 meters
}
