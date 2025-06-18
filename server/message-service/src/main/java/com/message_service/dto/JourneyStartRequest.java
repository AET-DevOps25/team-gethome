package com.message_service.dto;

import com.message_service.model.Location;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JourneyStartRequest {
    private Location startLocation;
    private Location endLocation;
    private double lengthInMeters;
}
