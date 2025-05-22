package com.authservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResendVerificationRequest {
    private String email;

    public ResendVerificationRequest() {
    }

    public ResendVerificationRequest(String email) {
        this.email = email;
    }
} 