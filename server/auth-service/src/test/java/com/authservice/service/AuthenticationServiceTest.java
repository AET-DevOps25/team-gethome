package com.authservice.service;

import com.authservice.dto.*;
import com.authservice.model.AuthProvider;
import com.authservice.model.Role;
import com.authservice.model.User;
import com.authservice.repository.UserRepository;
import com.authservice.service.JwtService;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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

    @InjectMocks
    private AuthenticationService authenticationService;

    private User testUser;
    private RegisterRequest registerRequest;
    private AuthenticationRequest authenticationRequest;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationService(
                userRepository,
                passwordEncoder,
                jwtService,
                authenticationManager,
                emailService
        );

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
        registerRequest.setPassword("password");

        authenticationRequest = new AuthenticationRequest();
        authenticationRequest.setEmail("test@example.com");
        authenticationRequest.setPassword("password");
    }

    @Test
    void register_ShouldCreateNewUser() throws MessagingException {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwtToken");
        doNothing().when(emailService).sendVerificationEmail(any(User.class));

        // Act
        AuthenticationResponse response = authenticationService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwtToken", response.getToken());
        assertEquals(registerRequest.getEmail(), response.getEmail());
        assertFalse(response.isEmailVerified());
        verify(userRepository).save(any(User.class));
        verify(emailService).sendVerificationEmail(any(User.class));
    }

    @Test
    void register_WhenEmailExists_ShouldThrowException() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authenticationService.register(registerRequest));
    }

    @Test
    void authenticate_ShouldReturnToken() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(any(User.class))).thenReturn("jwtToken");

        // Act
        AuthenticationResponse response = authenticationService.authenticate(authenticationRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwtToken", response.getToken());
        assertEquals(authenticationRequest.getEmail(), response.getEmail());
        assertTrue(response.isEmailVerified());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void verifyEmail_ShouldUpdateUser() {
        lenient().when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        lenient().when(jwtService.isTokenValid(anyString(), any(User.class))).thenReturn(true);
        lenient().when(userRepository.save(any(User.class))).thenReturn(testUser);
    
        authenticationService.verifyEmail(new VerifyEmailRequest("validToken"));
    
        assertTrue(testUser.isEmailVerified());
        verify(userRepository).save(any(User.class));
    }
    

    @Test
    void resendVerificationEmail_ShouldSendEmail() throws MessagingException {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        doNothing().when(emailService).sendVerificationEmail(any(User.class));

        // Act
        authenticationService.resendVerificationEmail(new ResendVerificationRequest("test@example.com"));

        // Assert
        verify(emailService).sendVerificationEmail(testUser);
    }

    @Test
    void sendPasswordResetEmail_ShouldSendEmail() throws MessagingException {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        doNothing().when(emailService).sendPasswordResetEmail(any(User.class));

        // Act
        authenticationService.sendPasswordResetEmail(new ForgotPasswordRequest("test@example.com"));

        // Assert
        verify(emailService).sendPasswordResetEmail(testUser);
    }

    @Test
    void resetPassword_ShouldUpdatePassword() {
        // Arrange
        lenient().when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        lenient().when(jwtService.isTokenValid(anyString(), any(User.class))).thenReturn(true);
        lenient().when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");
        lenient().when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        authenticationService.resetPassword(new ResetPasswordRequest("validToken", "newPassword"));

        // Assert
        assertEquals("newEncodedPassword", testUser.getPassword());
        verify(userRepository).save(any(User.class)); // ensure matchers match
    }
} 