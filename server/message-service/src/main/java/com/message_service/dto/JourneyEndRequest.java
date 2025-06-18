package com.message_service.dto;

import com.message_service.model.Location;

public record JourneyEndRequest(
        Location endLocation,
        String      notes
) { }
