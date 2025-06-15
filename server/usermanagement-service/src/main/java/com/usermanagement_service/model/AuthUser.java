package com.usermanagement_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class AuthUser {
    @Id
    private String id;
    private String email;
    private String password;
    private String provider;
    private String providerId;
    private boolean enabled;
    private boolean emailVerified;
} 