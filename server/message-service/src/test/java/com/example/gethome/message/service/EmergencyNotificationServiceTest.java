package com.example.gethome.message.service;

import com.example.gethome.message.client.UserManagementClient;
import com.example.gethome.message.dto.EmergencyNotificationRequest;
import com.example.gethome.message.dto.EmergencyNotificationResponse;
import com.example.gethome.message.model.EmergencyNotification;
import com.example.gethome.message.model.MessageLog;
import com.example.gethome.message.repository.EmergencyNotificationRepository;
import com.example.gethome.message.repository.MessageLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

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

    @Mock
    private MessageTemplateService messageTemplateService;

    @InjectMocks
    private EmergencyNotificationService emergencyNotificationService;

    private EmergencyNotificationRequest request;
    private List<UserManagementClient.EmergencyContact> emergencyContacts;
    private EmergencyNotification mockNotification;

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

        emergencyContacts = Arrays.asList(
            new UserManagementClient.EmergencyContact("contact-1", "John Doe", "john@example.com", "+1234567890", "EMAIL"),
            new UserManagementClient.EmergencyContact("contact-2", "Jane Smith", "jane@example.com", "+0987654321", "SMS")
        );

        // Create contact notifications for the mock
        List<EmergencyNotification.ContactNotification> contactNotifications = Arrays.asList(
            EmergencyNotification.ContactNotification.builder()
                .contactId("contact-1")
                .contactName("John Doe")
                .contactEmail("john@example.com")
                .contactPhone("+1234567890")
                .preferredMethod(EmergencyNotification.ContactNotification.PreferredContactMethod.EMAIL)
                .status(EmergencyNotification.NotificationStatus.SENT)
                .sentAt(LocalDateTime.now())
                .deliveryId("email-delivery-id")
                .build(),
            EmergencyNotification.ContactNotification.builder()
                .contactId("contact-2")
                .contactName("Jane Smith")
                .contactEmail("jane@example.com")
                .contactPhone("+0987654321")
                .preferredMethod(EmergencyNotification.ContactNotification.PreferredContactMethod.SMS)
                .status(EmergencyNotification.NotificationStatus.SENT)
                .sentAt(LocalDateTime.now())
                .deliveryId("sms-delivery-id")
                .build()
        );

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
            .contactNotifications(contactNotifications)
            .metadata(java.util.Map.of("message", "Emergency test message"))
            .build();
    }

    @Test
    void sendEmergencyNotification_Success() {
        // Given
        when(userManagementClient.getEmergencyContacts(anyString()))
            .thenReturn(emergencyContacts);
        when(emergencyNotificationRepository.save(any(EmergencyNotification.class)))
            .thenReturn(mockNotification);
        when(emailService.sendEmergencyEmail(any(), any()))
            .thenReturn("email-delivery-id");
        when(smsService.sendEmergencySms(any(), any()))
            .thenReturn("sms-delivery-id");

        // Calculate expected log count
        int expectedLogCount = 0;
        for (UserManagementClient.EmergencyContact contact : emergencyContacts) {
            String method = contact.preferredMethod().toUpperCase();
            if ("BOTH".equals(method)) {
                expectedLogCount += 2;
            } else {
                expectedLogCount += 1;
            }
        }

        // When
        EmergencyNotificationResponse response = emergencyNotificationService.sendEmergencyNotification(request, "auth-token");

        // Then
        assertNotNull(response);
        assertEquals("notification-1", response.getId());
        assertEquals("test-user", response.getUserId());
        assertEquals("SENT", response.getStatus());
        verify(emergencyNotificationRepository, times(2)).save(any(EmergencyNotification.class));
        verify(messageLogRepository, times(expectedLogCount)).save(any(MessageLog.class));
    }

    @Test
    void sendEmergencyNotification_NoContacts() {
        // Given
        when(userManagementClient.getEmergencyContacts(anyString()))
            .thenReturn(Arrays.asList());

        // When
        EmergencyNotificationResponse response = emergencyNotificationService.sendEmergencyNotification(request, "auth-token");

        // Then
        assertNotNull(response);
        assertEquals("no-contacts", response.getId());
        assertEquals("NO_CONTACTS", response.getStatus());
        assertEquals("No emergency contacts found", response.getMessage());
        verify(emergencyNotificationRepository, never()).save(any());
    }

    @Test
    void getUserNotifications_Success() {
        // Given
        List<EmergencyNotification> notifications = Arrays.asList(mockNotification);
        when(emergencyNotificationRepository.findByUserId("test-user"))
            .thenReturn(notifications);

        // When
        List<EmergencyNotificationResponse> responses = emergencyNotificationService.getUserNotifications("test-user");

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("notification-1", responses.get(0).getId());
    }

    @Test
    void getNotification_Success() {
        // Given
        when(emergencyNotificationRepository.findById("notification-1"))
            .thenReturn(java.util.Optional.of(mockNotification));

        // When
        EmergencyNotificationResponse response = emergencyNotificationService.getNotification("notification-1", "test-user");

        // Then
        assertNotNull(response);
        assertEquals("notification-1", response.getId());
    }

    @Test
    void getNotification_AccessDenied() {
        // Given
        when(emergencyNotificationRepository.findById("notification-1"))
            .thenReturn(java.util.Optional.of(mockNotification));

        // When & Then
        assertThrows(RuntimeException.class, () -> 
            emergencyNotificationService.getNotification("notification-1", "different-user"));
    }
} 