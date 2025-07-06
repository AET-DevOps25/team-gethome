package com.example.routing_service.dto;

import com.example.routing_service.model.Flag;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlagResponse {
    private String id;
    private double latitude;
    private double longitude;
    private String reason;
    private LocalDateTime timestamp;
    private String reportedBy;
    
    public static FlagResponse fromFlag(Flag flag) {
        return new FlagResponse(
            flag.getId(),
            flag.getLatitude(),
            flag.getLongitude(),
            flag.getReason(),
            flag.getTimestamp(),
            flag.getReportedBy()
        );
    }
}
