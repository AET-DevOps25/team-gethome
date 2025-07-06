package com.authservice.controller;

import com.authservice.dto.*;
import com.authservice.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AuthenticationService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void verifyEmail_ShouldReturnSuccessMessage() {
        // Arrange
        String token = "verificationToken";
        doNothing().when(authService).verifyEmail(any(VerifyEmailRequest.class));

        // Act
        ResponseEntity<String> result = authController.verifyEmail(new VerifyEmailRequest(token));

        // Assert
        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());
        assertEquals("Email verified successfully", result.getBody());
        verify(authService).verifyEmail(any(VerifyEmailRequest.class));
    }

    @Test
    void resendVerification_ShouldReturnSuccessMessage() {
        // Arrange
        String email = "test@example.com";
        doNothing().when(authService).resendVerificationEmail(any(ResendVerificationRequest.class));

        // Act
        ResponseEntity<String> result = authController.resendVerification(new ResendVerificationRequest(email));

        // Assert
        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());
        assertEquals("Verification email sent", result.getBody());
        verify(authService).resendVerificationEmail(any(ResendVerificationRequest.class));
    }

    @Test
    void forgotPassword_ShouldReturnSuccessMessage() {
        // Arrange
        String email = "test@example.com";
        doNothing().when(authService).sendPasswordResetEmail(any(ForgotPasswordRequest.class));

        // Act
        ResponseEntity<String> result = authController.forgotPassword(new ForgotPasswordRequest(email));

        // Assert
        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());
        assertEquals("Password reset email sent", result.getBody());
        verify(authService).sendPasswordResetEmail(any(ForgotPasswordRequest.class));
    }

    @Test
    void resetPassword_ShouldReturnSuccessMessage() {
        // Arrange
        String token = "resetToken";
        String newPassword = "newPassword123";
        doNothing().when(authService).resetPassword(any(ResetPasswordRequest.class));

        // Act
        ResponseEntity<String> result = authController.resetPassword(new ResetPasswordRequest(token, newPassword));

        // Assert
        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());
        assertEquals("Password reset successfully", result.getBody());
        verify(authService).resetPassword(any(ResetPasswordRequest.class));
    }
} 