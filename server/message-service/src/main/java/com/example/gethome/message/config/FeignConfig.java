package com.example.gethome.message.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
@Slf4j
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                log.debug("FeignConfig interceptor applying to: {}", template.url());
                
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    String authorizationHeader = request.getHeader("Authorization");
                    
                    log.debug("Original request URI: {}", request.getRequestURI());
                    log.debug("Authorization header present: {}", authorizationHeader != null);
                    
                    if (authorizationHeader != null) {
                        log.debug("Forwarding Authorization header to: {}", template.url());
                        template.header("Authorization", authorizationHeader);
                    } else {
                        log.warn("No Authorization header found in request to forward to: {}", template.url());
                    }
                } else {
                    log.warn("No request context available for forwarding Authorization header to: {}", template.url());
                }
            }
        };
    }
} 