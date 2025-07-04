package com.authservice.service;

import com.authservice.dto.*;
import com.authservice.model.User;
import com.authservice.model.Role;
import com.authservice.model.AuthProvider;
import com.authservice.repository.UserRepository;
import com.authservice.security.JwtService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        var user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)  // Set default role
                .provider(AuthProvider.LOCAL)
                .emailVerified(true)
                //.emailVerified(false)
                .build();

        userRepository.save(user);
        //try {
        //    emailService.sendVerificationEmail(user);
        //} catch (MessagingException e) {
        //    throw new RuntimeException("Failed to send verification email", e);
        //}

        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .email(user.getEmail())
                .name(user.getName())
                .enabled(user.isEnabled())
                .emailVerified(true)
                //.emailVerified(user.isEmailVerified())
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
            )
        );
        var user = userRepository.findByEmail(request.getEmail())
            .orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
            .token(jwtToken)
            .build();
    }

    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        String token = request.getToken();
        String email = jwtService.extractClaim(token, claims -> claims.get("email", String.class));
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (!jwtService.isTokenValid(token, user)) {
            throw new RuntimeException("Invalid token");
        }

        user.setEmailVerified(true);
        userRepository.save(user);
    }

    public void resendVerificationEmail(ResendVerificationRequest request) {
        String email = request.getEmail();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isEmailVerified()) {
            throw new RuntimeException("Email already verified");
        }

        try {
            emailService.sendVerificationEmail(user);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    public void sendPasswordResetEmail(ForgotPasswordRequest request) {
        String email = request.getEmail();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            emailService.sendPasswordResetEmail(user);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String token = request.getToken();
        String newPassword = request.getNewPassword();
        String email = jwtService.extractClaim(token, claims -> claims.get("email", String.class));
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (!jwtService.isTokenValid(token, user)) {
            throw new RuntimeException("Invalid token");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public AuthenticationResponse refreshToken(String token) {
        String userId = jwtService.extractUserId(token);
        String email = jwtService.extractClaim(token, claims -> claims.get("email", String.class));
        
        if (userId == null || email == null) {
            throw new RuntimeException("Invalid token");
        }

        var user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        if (!jwtService.isTokenValid(token, user)) {
            throw new RuntimeException("Invalid token");
        }

        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
            .token(jwtToken)
            .build();
    }

    public void validateToken(String token) {
        String userId = jwtService.extractUserId(token);
        String email = jwtService.extractClaim(token, claims -> claims.get("email", String.class));
        
        if (userId == null || email == null) {
            throw new RuntimeException("Invalid token");
        }

        var user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        if (!jwtService.isTokenValid(token, user)) {
            throw new RuntimeException("Invalid token");
        }
    }

    public void verifyEmail(String token) {
        String userId = jwtService.extractUserId(token);
        String email = jwtService.extractClaim(token, claims -> claims.get("email", String.class));
        
        if (userId == null || email == null) {
            throw new RuntimeException("Invalid token");
        }

        var user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        if (!jwtService.isTokenValid(token, user)) {
            throw new RuntimeException("Invalid token");
        }

        user.setEmailVerified(true);
        userRepository.save(user);
    }

    public void resetPassword(String token, String newPassword) {
        String userId = jwtService.extractUserId(token);
        String email = jwtService.extractClaim(token, claims -> claims.get("email", String.class));
        
        if (userId == null || email == null) {
            throw new RuntimeException("Invalid token");
        }

        var user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        if (!jwtService.isTokenValid(token, user)) {
            throw new RuntimeException("Invalid token");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private AuthenticationResponse buildAuthenticationResponse(User user, String token) {
        return AuthenticationResponse.builder()
            .token(token)
            .build();
    }
}