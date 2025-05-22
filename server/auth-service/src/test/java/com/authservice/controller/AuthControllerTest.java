package com.authservice.controller;

import com.authservice.dto.*;
import com.authservice.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController(authService);
    }

    @Test
    public void testRegister() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Test User");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        AuthenticationResponse response = AuthenticationResponse.builder()
                .token("jwt-token")
                .email("test@example.com")
                .emailVerified(false)
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.emailVerified").value(false));
    }

    @Test
    public void testAuthenticate() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        AuthenticationResponse response = AuthenticationResponse.builder()
                .token("jwt-token")
                .email("test@example.com")
                .emailVerified(true)
                .build();

        when(authService.authenticate(any(AuthenticationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.emailVerified").value(true));
    }

    @Test
    void verifyEmail_ShouldReturnSuccessMessage() {
        // Arrange
        String token = "verificationToken";

        // Act
        ResponseEntity<String> result = authController.verifyEmail(new VerifyEmailRequest(token));

        // Assert
        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());
        assertEquals("Email verified successfully", result.getBody());
        verify(authService).verifyEmail(new VerifyEmailRequest(token));
    }

    @Test
    void resendVerification_ShouldReturnSuccessMessage() {
        // Arrange
        String email = "test@example.com";

        // Act
        ResponseEntity<String> result = authController.resendVerification(new ResendVerificationRequest(email));

        // Assert
        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());
        assertEquals("Verification email sent", result.getBody());
        verify(authService).resendVerificationEmail(new ResendVerificationRequest(email));
    }

    @Test
    void forgotPassword_ShouldReturnSuccessMessage() {
        // Arrange
        String email = "test@example.com";

        // Act
        ResponseEntity<String> result = authController.forgotPassword(new ForgotPasswordRequest(email));

        // Assert
        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());
        assertEquals("Password reset email sent", result.getBody());
        verify(authService).sendPasswordResetEmail(new ForgotPasswordRequest(email));
    }

    @Test
    void resetPassword_ShouldReturnSuccessMessage() {
        // Arrange
        String token = "resetToken";
        String newPassword = "newPassword123";

        // Act
        ResponseEntity<String> result = authController.resetPassword(new ResetPasswordRequest(token, newPassword));

        // Assert
        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());
        assertEquals("Password reset successfully", result.getBody());
        verify(authService).resetPassword(new ResetPasswordRequest(token, newPassword));
    }
} 