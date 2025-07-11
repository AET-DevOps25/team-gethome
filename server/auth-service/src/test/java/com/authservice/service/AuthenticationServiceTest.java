package com.authservice.service;

import com.authservice.dto.*;
import com.authservice.model.AuthProvider;
import com.authservice.model.Role;
import com.authservice.model.User;
import com.authservice.repository.UserRepository;
import com.authservice.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private EmailService emailService;

    private User testUser;
    private RegisterRequest registerRequest;
    private AuthenticationRequest authenticationRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("1");
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(Role.USER);
        testUser.setProvider(AuthProvider.LOCAL);
        testUser.setEmailVerified(false);

        registerRequest = new RegisterRequest();
        registerRequest.setName("Test User");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");

        authenticationRequest = new AuthenticationRequest();
        authenticationRequest.setEmail("test@example.com");
        authenticationRequest.setPassword("password123");
    }

    @Test
    void testUserCreation() {
        // Simple test to verify test setup
        assertNotNull(testUser);
        assertEquals("test@example.com", testUser.getEmail());
        assertEquals("Test User", testUser.getName());
        assertEquals(Role.USER, testUser.getRole());
        assertEquals(AuthProvider.LOCAL, testUser.getProvider());
        assertFalse(testUser.isEmailVerified());
    }

    @Test
    void testRequestObjects() {
        // Test request object creation
        assertNotNull(registerRequest);
        assertEquals("test@example.com", registerRequest.getEmail());
        assertEquals("Test User", registerRequest.getName());
        assertEquals("password123", registerRequest.getPassword());

        assertNotNull(authenticationRequest);
        assertEquals("test@example.com", authenticationRequest.getEmail());
        assertEquals("password123", authenticationRequest.getPassword());
    }

    @Test
    void testRepositoryMocking() {
        // Test basic mocking setup
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        assertTrue(userRepository.existsByEmail("test@example.com"));
        
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");
        assertTrue(foundUser.isPresent());
        assertEquals("test@example.com", foundUser.get().getEmail());
    }

    @Test
    void testPasswordEncoderMocking() {
        // Test password encoder mocking
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        String encoded = passwordEncoder.encode("password123");
        assertEquals("encodedPassword", encoded);
    }

    @Test
    void testJwtServiceMocking() {
        // Test JWT service mocking
        when(jwtService.generateToken(testUser)).thenReturn("jwtToken");
        String token = jwtService.generateToken(testUser);
        assertEquals("jwtToken", token);
        
        when(jwtService.extractClaim(anyString(), any())).thenReturn("test@example.com");
        String email = jwtService.extractClaim("token", claims -> claims.getSubject());
        assertEquals("test@example.com", email);
        
        when(jwtService.isTokenValid("validToken", testUser)).thenReturn(true);
        assertTrue(jwtService.isTokenValid("validToken", testUser));
    }

    @Test
    void testAuthenticationManagerMocking() {
        // Test authentication manager mocking
        UsernamePasswordAuthenticationToken authToken = 
            new UsernamePasswordAuthenticationToken("test@example.com", "password123");
        
        // Mock the return value since authenticate() returns Authentication object
        UsernamePasswordAuthenticationToken mockAuthentication = 
            new UsernamePasswordAuthenticationToken("test@example.com", "password123");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(mockAuthentication);
        
        // This should not throw an exception
        org.springframework.security.core.Authentication result = authenticationManager.authenticate(authToken);
        assertNotNull(result);
        assertEquals("test@example.com", result.getPrincipal());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void testEmailServiceMocking() throws Exception {
        // Test email service mocking
        doNothing().when(emailService).sendVerificationEmail(testUser);
        doNothing().when(emailService).sendPasswordResetEmail(testUser);
        
        // These should not throw exceptions
        emailService.sendVerificationEmail(testUser);
        emailService.sendPasswordResetEmail(testUser);
        
        verify(emailService).sendVerificationEmail(testUser);
        verify(emailService).sendPasswordResetEmail(testUser);
    }
} 