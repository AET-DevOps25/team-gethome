package com.message_service.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "user-management")
public class UserManagementProperties {
    private String url;
}