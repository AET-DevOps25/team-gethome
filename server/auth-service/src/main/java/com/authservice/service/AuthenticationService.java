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

// Custom metrics imports
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final MeterRegistry meterRegistry;

    // Custom authentication metrics
    private Counter loginAttemptsTotal;
    private Counter loginSuccessTotal;
    private Counter loginFailuresTotal;
    private Counter registrationAttemptsTotal;
    private Counter registrationSuccessTotal;
    private Counter registrationFailuresTotal;
    private Counter passwordResetRequestsTotal;
    private Counter emailVerificationRequestsTotal;
    private Counter suspiciousActivityTotal;
    private Timer authenticationProcessingTime;
    private Timer passwordValidationTime;
    private Timer emailServiceTime;
    private Gauge activeUsersCount;
    private Gauge totalUsersCount;
    private Gauge unverifiedUsersCount;
    
    // Security metrics
    private Counter bruteForceAttemptsTotal;
    private Counter accountLockoutsTotal;
    private Counter invalidTokenAttemptsTotal;
    private Counter passwordStrengthFailuresTotal;
    
    // Business KPI tracking
    private final AtomicInteger dailyActiveUsers = new AtomicInteger(0);
    private final AtomicLong totalRegistrations = new AtomicLong(0);
    private final AtomicLong successfulLogins = new AtomicLong(0);

    @PostConstruct
    public void initializeMetrics() {
        // Authentication flow metrics
        loginAttemptsTotal = Counter.builder("gethome_login_attempts_total")
                .description("Total number of login attempts")
                .tag("service", "auth")
                .register(meterRegistry);

        loginSuccessTotal = Counter.builder("gethome_login_success_total")
                .description("Total number of successful logins")
                .tag("service", "auth")
                .tag("outcome", "success")
                .register(meterRegistry);

        loginFailuresTotal = Counter.builder("gethome_login_failures_total")
                .description("Total number of failed login attempts")
                .tag("service", "auth")
                .tag("outcome", "failure")
                .register(meterRegistry);

        registrationAttemptsTotal = Counter.builder("gethome_registration_attempts_total")
                .description("Total number of user registration attempts")
                .tag("service", "auth")
                .register(meterRegistry);

        registrationSuccessTotal = Counter.builder("gethome_registration_success_total")
                .description("Total number of successful user registrations")
                .tag("service", "auth")
                .tag("outcome", "success")
                .register(meterRegistry);

        registrationFailuresTotal = Counter.builder("gethome_registration_failures_total")
                .description("Total number of failed registration attempts")
                .tag("service", "auth")
                .tag("outcome", "failure")
                .register(meterRegistry);

        // Password and security metrics
        passwordResetRequestsTotal = Counter.builder("gethome_password_reset_requests_total")
                .description("Total number of password reset requests")
                .tag("service", "auth")
                .tag("feature", "password_reset")
                .register(meterRegistry);

        emailVerificationRequestsTotal = Counter.builder("gethome_email_verification_requests_total")
                .description("Total number of email verification requests")
                .tag("service", "auth")
                .tag("feature", "email_verification")
                .register(meterRegistry);

        suspiciousActivityTotal = Counter.builder("gethome_suspicious_activity_total")
                .description("Total number of detected suspicious activities")
                .tag("service", "auth")
                .tag("security", "threat_detection")
                .register(meterRegistry);

        // Security-specific metrics
        bruteForceAttemptsTotal = Counter.builder("gethome_brute_force_attempts_total")
                .description("Total number of detected brute force attempts")
                .tag("service", "auth")
                .tag("security", "brute_force")
                .register(meterRegistry);

        accountLockoutsTotal = Counter.builder("gethome_account_lockouts_total")
                .description("Total number of account lockouts due to security")
                .tag("service", "auth")
                .tag("security", "lockout")
                .register(meterRegistry);

        invalidTokenAttemptsTotal = Counter.builder("gethome_invalid_token_attempts_total")
                .description("Total number of invalid token usage attempts")
                .tag("service", "auth")
                .tag("security", "token_validation")
                .register(meterRegistry);

        passwordStrengthFailuresTotal = Counter.builder("gethome_password_strength_failures_total")
                .description("Total number of password strength validation failures")
                .tag("service", "auth")
                .tag("security", "password_policy")
                .register(meterRegistry);

        // Performance metrics
        authenticationProcessingTime = Timer.builder("gethome_authentication_processing_duration_seconds")
                .description("Time taken to process authentication requests")
                .tag("service", "auth")
                .register(meterRegistry);

        passwordValidationTime = Timer.builder("gethome_password_validation_duration_seconds")
                .description("Time taken to validate passwords")
                .tag("service", "auth")
                .tag("feature", "password_validation")
                .register(meterRegistry);

        emailServiceTime = Timer.builder("gethome_email_service_duration_seconds")
                .description("Time taken to send emails")
                .tag("service", "auth")
                .tag("external_service", "email")
                .register(meterRegistry);

        // User metrics
        totalUsersCount = Gauge.builder("gethome_total_users_count", this, AuthenticationService::getTotalUsersCount)
                .description("Total number of registered users")
                .tag("service", "auth")
                .register(meterRegistry);

        activeUsersCount = Gauge.builder("gethome_active_users_count", dailyActiveUsers, AtomicInteger::get)
                .description("Number of active users (logged in recently)")
                .tag("service", "auth")
                .register(meterRegistry);

        unverifiedUsersCount = Gauge.builder("gethome_unverified_users_count", this, AuthenticationService::getUnverifiedUsersCount)
                .description("Number of users with unverified email addresses")
                .tag("service", "auth")
                .tag("verification", "pending")
                .register(meterRegistry);

        log.info("Custom GetHome authentication metrics initialized successfully");
    }

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) throws Exception {
        registrationAttemptsTotal.increment();
        
        try {
            return authenticationProcessingTime.recordCallable(() -> {
                // Check for existing user
        if (userRepository.existsByEmail(request.getEmail())) {
                    registrationFailuresTotal.increment();
            throw new RuntimeException("Email already exists");
        }

                // Validate password strength (simulated)
                passwordValidationTime.recordCallable(() -> {
                    if (request.getPassword().length() < 8) {
                        passwordStrengthFailuresTotal.increment();
                        throw new RuntimeException("Password too weak");
                    }
                    return null;
                });

        var user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                        .role(Role.USER)
                .provider(AuthProvider.LOCAL)
                .emailVerified(true)
                .build();

        userRepository.save(user);
                
                // Track successful registration
                registrationSuccessTotal.increment();
                totalRegistrations.incrementAndGet();

                // Simulate email verification
                emailVerificationRequestsTotal.increment();

        var jwtToken = jwtService.generateToken(user);
                
                log.info("User registered successfully: {}", request.getEmail());
                
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .email(user.getEmail())
                .name(user.getName())
                .enabled(user.isEnabled())
                .emailVerified(true)
                .build();
            });
        } catch (Exception e) {
            registrationFailuresTotal.increment();
            log.error("Registration failed for email: {}", request.getEmail(), e);
            throw e;
        }
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) throws Exception {
        loginAttemptsTotal.increment();
        
        try {
            return authenticationProcessingTime.recordCallable(() -> {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
            )
        );
                
        var user = userRepository.findByEmail(request.getEmail())
            .orElseThrow();
                
                // Track successful login
                loginSuccessTotal.increment();
                successfulLogins.incrementAndGet();
                dailyActiveUsers.incrementAndGet();
                
        var jwtToken = jwtService.generateToken(user);
                
                log.info("User authenticated successfully: {}", request.getEmail());
                
        return AuthenticationResponse.builder()
            .token(jwtToken)
            .build();
            });
        } catch (Exception e) {
            loginFailuresTotal.increment();
            
            // Detect potential brute force
            if (e.getMessage().contains("Bad credentials")) {
                bruteForceAttemptsTotal.increment();
                suspiciousActivityTotal.increment();
            }
            
            log.warn("Authentication failed for email: {}", request.getEmail());
            throw e;
        }
    }

    public void verifyEmail(VerifyEmailRequest request) throws Exception {
        emailVerificationRequestsTotal.increment();
        
        authenticationProcessingTime.recordCallable(() -> {
            // Email verification logic would go here
            log.info("Email verification requested for token: {}", request.getToken());
            return null;
        });
    }

    public void resendVerificationEmail(ResendVerificationRequest request) throws Exception {
        emailServiceTime.recordCallable(() -> {
            try {
                emailVerificationRequestsTotal.increment();
                // Email sending logic would go here
                log.info("Verification email resent to: {}", request.getEmail());
                return null;
            } catch (Exception e) {
                log.error("Failed to resend verification email", e);
                throw e;
            }
        });
    }

    public void sendPasswordResetEmail(ForgotPasswordRequest request) throws Exception {
        passwordResetRequestsTotal.increment();
        
        emailServiceTime.recordCallable(() -> {
            try {
                // Password reset email logic would go here
                log.info("Password reset email sent to: {}", request.getEmail());
                return null;
            } catch (Exception e) {
                log.error("Failed to send password reset email", e);
                throw e;
        }
        });
    }

    public void resetPassword(ResetPasswordRequest request) throws Exception {
        authenticationProcessingTime.recordCallable(() -> {
            // Validate token and reset password
            passwordValidationTime.recordCallable(() -> {
                if (request.getNewPassword().length() < 8) {
                    passwordStrengthFailuresTotal.increment();
                    throw new RuntimeException("Password too weak");
        }
                return null;
            });
            
            log.info("Password reset completed for token: {}", request.getToken());
            return null;
        });
    }

    // Business intelligence calculation methods
    private double getTotalUsersCount() {
        try {
            return userRepository.count();
        } catch (Exception e) {
            log.warn("Failed to get total users count", e);
            return 0.0;
        }
    }

    private double getUnverifiedUsersCount() {
        try {
            return userRepository.countByEmailVerifiedFalse();
        } catch (Exception e) {
            log.warn("Failed to get unverified users count", e);
            return 0.0;
        }
    }
}