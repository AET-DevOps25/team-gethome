package com.usermanagement_service.dto;

import com.usermanagement_service.model.PreferredContactMethod;
import java.util.UUID;

public record EmergencyContactData(
    UUID userId,
    String alias,
    PreferredContactMethod preferredContactMethod,
    String email,
    String phone
) {} 