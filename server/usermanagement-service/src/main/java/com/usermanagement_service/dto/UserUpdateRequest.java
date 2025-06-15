package com.usermanagement_service.dto;

import com.usermanagement_service.model.*;
import java.util.List;

public record UserUpdateRequest(
    String alias,
    Gender gender,
    AgeGroup ageGroup,
    Preferences preferences,
    PreferredContactMethod preferredContactMethod,
    String phoneNr,
    List<EmergencyContactDTO> emergencyContacts
) {} 