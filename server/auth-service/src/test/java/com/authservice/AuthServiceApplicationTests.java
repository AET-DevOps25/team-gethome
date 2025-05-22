package com.authservice;

import com.authservice.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@Import(TestConfig.class)
@TestPropertySource(properties = {
    "spring.mail.username=test@example.com",
    "spring.mail.password=test-password",
    "jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970",
    "jwt.expiration=86400000",
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AuthServiceApplicationTests {

    @Test
    void contextLoads() {
    }
} 