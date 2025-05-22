package com.authservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyEmailRequest {
    private String token;

    public VerifyEmailRequest() {
    }

    public VerifyEmailRequest(String token) {
        this.token = token;
    }
} 