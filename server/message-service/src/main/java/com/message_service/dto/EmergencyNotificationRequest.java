package com.message_service.dto;

import com.message_service.model.IssuedBy;
import com.message_service.model.Location;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyNotificationRequest {
    private Location location;
    private IssuedBy issuedBy; // AI || USER
    private String context; // When AI calls, it gives summary, why emergency
}