package com.authservice.service;

import com.authservice.model.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private EmailService emailService;

    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        JavaMailSenderImpl realMailSender = new JavaMailSenderImpl();
        mimeMessage = realMailSender.createMimeMessage();

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Processed Template</html>");
    }

    @Test
    void sendVerificationEmail_ShouldSendEmail() throws MessagingException {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");
        user.setName("Test User");

        when(jwtService.generateToken(user)).thenReturn("jwtToken");

        // Act
        emailService.sendVerificationEmail(user);

        // Assert
        verify(jwtService).generateToken(user);
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendPasswordResetEmail_ShouldSendEmail() throws MessagingException {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");
        user.setName("Test User");

        when(jwtService.generateToken(user)).thenReturn("jwtToken");

        // Act
        emailService.sendPasswordResetEmail(user);

        // Assert
        verify(jwtService).generateToken(user);
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendEmail_ShouldSendEmail() throws MessagingException {
        // Arrange
        String to = "test@example.com";
        String subject = "Test Subject";
        String content = "Test Content";

        // Act
        emailService.sendEmail(to, subject, content);

        // Assert
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendEmail_WhenMessagingException_ShouldThrowException() throws MessagingException {
        // Arrange
        doThrow(new MessagingException("Failed to send email"))
                .when(mailSender).send(any(MimeMessage.class));

        // Act & Assert
        assertThrows(MessagingException.class, () ->
            emailService.sendEmail("test@example.com", "Test Subject", "Test Content")
        );
    }
}
