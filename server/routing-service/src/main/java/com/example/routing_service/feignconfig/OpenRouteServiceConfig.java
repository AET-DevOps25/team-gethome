package com.example.routing_service.feignconfig;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;


@Slf4j
public class OpenRouteServiceConfig implements RequestInterceptor {
    @Value("${routing.api.key}")
    private String openRouteServiceApiKey;

    @Override
    public void apply(RequestTemplate template) {
        log.info("=== OpenRoute Request Debug ===");
        log.info("URL: {}", template.url());
        log.info("Method: {}", template.method());
        log.info("API Key value: {}", openRouteServiceApiKey != null ? "***" + openRouteServiceApiKey.substring(Math.max(0, openRouteServiceApiKey.length() - 10)) : "NULL");
        log.info("API Key length: {}", openRouteServiceApiKey != null ? openRouteServiceApiKey.length() : 0);
        log.info("Request body: {}", template.body() != null ? new String(template.body()) : "NULL");
        
        template.header("Authorization", openRouteServiceApiKey);
        
        log.info("All headers after adding Authorization:");
        template.headers().forEach((key, values) -> {
            log.info("Header '{}': {}", key, values);
        });
        log.info("=== End Request Debug ===");
    }
} 