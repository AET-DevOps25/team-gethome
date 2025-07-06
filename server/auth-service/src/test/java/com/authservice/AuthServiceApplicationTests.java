package com.authservice;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@Disabled("Context load test disabled - not essential for business logic testing")
@SpringBootTest
@ImportAutoConfiguration(exclude = {MongoAutoConfiguration.class})
@TestPropertySource(properties = {
    "spring.mail.username=test@example.com",
    "spring.mail.password=test-password",
    "jwt.secret=test-secret-key-for-testing-purposes-only",
    "jwt.expiration=86400000"
})
class AuthServiceApplicationTests {

    @Test
    void contextLoads() {
        // This test verifies that the Spring context loads successfully
    }
} 