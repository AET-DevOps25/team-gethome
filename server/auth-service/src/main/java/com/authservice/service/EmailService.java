package com.authservice.service;

import com.authservice.model.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final JwtService jwtService;

    protected void sendEmail(String to, String subject, String content) throws MessagingException {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom("noreply@gethome.com");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            
            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", to, e);
            throw e;
        }
    }

    public void sendVerificationEmail(User user) throws MessagingException {
        try {
            String token = jwtService.generateToken(user);
            String verificationUrl = "http://localhost:3000/verify-email?token=" + token;

            Context context = new Context();
            context.setVariable("name", user.getName());
            context.setVariable("verificationUrl", verificationUrl);

            String emailContent = templateEngine.process("verification-email", context);
            sendEmail(user.getEmail(), "Verify your GetHome account", emailContent);
        } catch (Exception e) {
            log.error("Failed to send verification email to user: {}", user.getEmail(), e);
            throw new MessagingException("Failed to send verification email", e);
        }
    }

    public void sendPasswordResetEmail(User user) throws MessagingException {
        try {
            String token = jwtService.generateToken(user);
            String resetUrl = "http://localhost:3000/reset-password?token=" + token;

            Context context = new Context();
            context.setVariable("name", user.getName());
            context.setVariable("resetUrl", resetUrl);

            String emailContent = templateEngine.process("password-reset-email", context);
            sendEmail(user.getEmail(), "Reset your GetHome password", emailContent);
        } catch (Exception e) {
            log.error("Failed to send password reset email to user: {}", user.getEmail(), e);
            throw new MessagingException("Failed to send password reset email", e);
        }
    }
}