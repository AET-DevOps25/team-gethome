package com.usermanagement_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthUserResponse {
    private String id;
    private String email;
    private String provider;
    private String providerId;
} 