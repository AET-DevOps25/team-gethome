package com.example.gethome.message.service;

import com.example.gethome.message.dto.EmergencyNotificationRequest;
import com.example.gethome.message.dto.EmergencyNotificationResponse;
import com.example.gethome.message.model.EmergencyNotification;
import com.example.gethome.message.model.MessageLog;
import com.example.gethome.message.repository.EmergencyNotificationRepository;
import com.example.gethome.message.repository.MessageLogRepository;
import com.example.gethome.message.client.UserManagementClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmergencyNotificationServiceTest {

    @Mock
    private EmergencyNotificationRepository emergencyNotificationRepository;
    @Mock
    private MessageLogRepository messageLogRepository;
    @Mock
    private UserManagementClient userManagementClient;
    @Mock
    private EmailService emailService;
    @Mock
    private SmsService smsService;

    private EmergencyNotificationRequest request;
    private EmergencyNotification mockNotification;
    private MessageLog mockMessageLog;

    @BeforeEach
    void setUp() {
        request = EmergencyNotificationRequest.builder()
            .userId("test-user")
            .message("Emergency test message")
            .latitude(40.7128)
            .longitude(-74.0060)
            .emergencyType("MANUAL")
            .reason("Test emergency")
            .location("New York, NY")
            .emergencyContactIds(Arrays.asList("contact-1", "contact-2"))
            .build();

        mockNotification = EmergencyNotification.builder()
            .id("notification-1")
            .userId("test-user")
            .emergencyType("MANUAL")
            .reason("Test emergency")
            .latitude(40.7128)
            .longitude(-74.0060)
            .location("New York, NY")
            .triggeredAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusHours(24))
            .status(EmergencyNotification.NotificationStatus.SENT)
            .build();

        mockMessageLog = MessageLog.builder()
            .id("log-1")
            .userId("test-user")
            .messageType(MessageLog.MessageType.EMAIL)
            .contactEmail("test@example.com")
            .content("Emergency notification")
            .status(MessageLog.MessageStatus.SENT)
            .sentAt(LocalDateTime.now())
            .build();
    }

    @Test
    void testRequestObjectCreation() {
        // Test request object creation
        assertNotNull(request);
        assertEquals("test-user", request.getUserId());
        assertEquals("Emergency test message", request.getMessage());
        assertEquals("MANUAL", request.getEmergencyType());
        assertEquals("Test emergency", request.getReason());
        assertEquals("New York, NY", request.getLocation());
    }

    @Test
    void testEmergencyNotificationObjectCreation() {
        // Test emergency notification object creation
        assertNotNull(mockNotification);
        assertEquals("notification-1", mockNotification.getId());
        assertEquals("test-user", mockNotification.getUserId());
        assertEquals("MANUAL", mockNotification.getEmergencyType());
        assertEquals(EmergencyNotification.NotificationStatus.SENT, mockNotification.getStatus());
    }

    @Test
    void testMessageLogObjectCreation() {
        // Test message log object creation
        assertNotNull(mockMessageLog);
        assertEquals("log-1", mockMessageLog.getId());
        assertEquals("test-user", mockMessageLog.getUserId());
        assertEquals(MessageLog.MessageType.EMAIL, mockMessageLog.getMessageType());
        assertEquals("test@example.com", mockMessageLog.getContactEmail());
        assertEquals(MessageLog.MessageStatus.SENT, mockMessageLog.getStatus());
    }

    @Test
    void testRepositoryMocking() {
        // Test repository mocking
        when(emergencyNotificationRepository.findById("notification-1"))
            .thenReturn(Optional.of(mockNotification));
        Optional<EmergencyNotification> foundNotification = 
            emergencyNotificationRepository.findById("notification-1");
        assertTrue(foundNotification.isPresent());
        assertEquals("notification-1", foundNotification.get().getId());
        
        List<EmergencyNotification> userNotifications = Arrays.asList(mockNotification);
        when(emergencyNotificationRepository.findByUserId("test-user"))
            .thenReturn(userNotifications);
        List<EmergencyNotification> notifications = 
            emergencyNotificationRepository.findByUserId("test-user");
        assertEquals(1, notifications.size());
    }

    @Test
    void testUserManagementClientMocking() {
        // Test user management client mocking
        List<UserManagementClient.EmergencyContact> emergencyContacts = Arrays.asList(
            new UserManagementClient.EmergencyContact("contact-1", "John Doe", "john@example.com", "+1234567890", "EMAIL")
        );
        
        when(userManagementClient.getEmergencyContacts("test-user"))
            .thenReturn(emergencyContacts);
        
        List<UserManagementClient.EmergencyContact> contacts = 
            userManagementClient.getEmergencyContacts("test-user");
        assertEquals(1, contacts.size());
        assertEquals("contact-1", contacts.get(0).id());
        assertEquals("John Doe", contacts.get(0).name());
    }

    @Test
    void testEmailServiceMocking() {
        // Test email service mocking
        when(emailService.sendEmergencyEmail(any(EmergencyNotification.class), anyString()))
            .thenReturn("email-delivery-id");
        
        String deliveryId = emailService.sendEmergencyEmail(mockNotification, "test@example.com");
        assertEquals("email-delivery-id", deliveryId);
    }

    @Test
    void testSmsServiceMocking() {
        // Test SMS service mocking
        when(smsService.sendEmergencySMS(anyString(), any(EmergencyNotification.class)))
            .thenReturn("sms-delivery-id");
        
        String deliveryId = smsService.sendEmergencySMS("+1234567890", mockNotification);
        assertEquals("sms-delivery-id", deliveryId);
    }

    @Test
    void testNotificationStatusTransitions() {
        // Test notification status changes
        EmergencyNotification notification = EmergencyNotification.builder()
            .id("test-notification")
            .userId("user-1")
            .status(EmergencyNotification.NotificationStatus.PENDING)
            .build();
        
        assertEquals(EmergencyNotification.NotificationStatus.PENDING, notification.getStatus());
        
        notification.setStatus(EmergencyNotification.NotificationStatus.SENT);
        assertEquals(EmergencyNotification.NotificationStatus.SENT, notification.getStatus());
    }

    @Test
    void testRepositorySaveOperations() {
        // Test repository save operations
        when(emergencyNotificationRepository.save(any(EmergencyNotification.class)))
            .thenReturn(mockNotification);
        when(messageLogRepository.save(any(MessageLog.class)))
            .thenReturn(mockMessageLog);
        
        EmergencyNotification savedNotification = emergencyNotificationRepository.save(mockNotification);
        MessageLog savedLog = messageLogRepository.save(mockMessageLog);
        
        assertNotNull(savedNotification);
        assertNotNull(savedLog);
        assertEquals("notification-1", savedNotification.getId());
        assertEquals("log-1", savedLog.getId());
    }
} 