package com.example.routing_service.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Value("${routing.api.key}")
    private String openRouteServiceApiKey;

    @Bean
    public RequestInterceptor openRouteServiceAuthInterceptor() {
        return template -> {
            if (template.url().contains("openrouteservice")) {
                template.header("Authorization", openRouteServiceApiKey);
            }
        };
    }
} 