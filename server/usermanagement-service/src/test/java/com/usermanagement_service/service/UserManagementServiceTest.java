package com.usermanagement_service.service;

import com.usermanagement_service.dto.*;
import com.usermanagement_service.model.*;
import com.usermanagement_service.repository.AuthUserRepository;
import com.usermanagement_service.repository.EmergencyContactRepository;
import com.usermanagement_service.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceTest {

    @Mock
    private AuthUserRepository authUserRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private EmergencyContactRepository emergencyContactRepository;

    @Mock
    private UserCodeGenerator userCodeGenerator;

    @Mock
    private ProfilePictureGenerator profilePictureGenerator;

    @InjectMocks
    private UserManagementService userManagementService;

    private String userId;
    private AuthUser testUser;
    private com.usermanagement_service.model.UserProfile testProfile;

    @BeforeEach
    void setUp() {
        userId = "test-user-123";
        
        testUser = AuthUser.builder()
            .id(userId)
            .email("test@example.com")
            .password("password")
            .provider("LOCAL")
            .providerId(userId)
            .enabled(true)
            .emailVerified(true)
            .build();

        Map<String, Object> prefsMap = new HashMap<>();
        prefsMap.put("shareLocation", true);
        prefsMap.put("notifyOnDelay", true);
        prefsMap.put("autoNotifyContacts", true);
        prefsMap.put("checkInInterval", 30);
        prefsMap.put("enableSOS", true);

        Preferences preferences = Preferences.builder()
            .shareLocation(true)
            .notifyOnDelay(true)
            .autoNotifyContacts(true)
            .checkInInterval(30)
            .enableSOS(true)
            .preferences(prefsMap)
            .build();

        testProfile = com.usermanagement_service.model.UserProfile.builder()
            .id("profile-123")
            .userId(userId)
            .alias("TestUser")
            .gender(Gender.NO_INFO)
            .ageGroup(AgeGroup.ADULT)
            .preferences(preferences)
            .preferredContactMethod(PreferredContactMethod.EMAIL)
            .phoneNr("1234567890")
            .profilePictureUrl("http://example.com/avatar.jpg")
            .build();
    }

    @Test
    void getUserProfile_UserExists_ReturnsProfile() {
        // Given
        when(authUserRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(testProfile));
        when(emergencyContactRepository.findByRequesterIdAndStatus(userId, RequestStatus.ACCEPTED))
            .thenReturn(java.util.List.of());

        // When
        UserProfileResponse profile = userManagementService.getUserProfile(userId);

        // Then
        assertNotNull(profile);
        assertEquals(userId, profile.getUserId());
        assertEquals("test@example.com", profile.getEmail());
        assertEquals("TestUser", profile.getAlias());
    }

    @Test
    void getUserProfile_UserNotExists_ThrowsException() {
        // Given
        when(authUserRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> userManagementService.getUserProfile(userId));
    }

    @Test
    void createUserProfile_Success() {
        // Given
        UserCreationRequest creationRequest = UserCreationRequest.builder()
            .id(userId)
            .alias("NewUser")
            .gender(Gender.NO_INFO)
            .ageGroup(AgeGroup.ADULT)
            .preferences(new HashMap<>())
            .preferredContactMethod(PreferredContactMethod.EMAIL)
            .phoneNr("1234567890")
            .profilePictureUrl("http://example.com/avatar.jpg")
            .build();

        when(authUserRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userProfileRepository.save(any(com.usermanagement_service.model.UserProfile.class)))
            .thenReturn(testProfile);

        // When
        UserProfileResponse profile = userManagementService.createUserProfile(creationRequest);

        // Then
        assertNotNull(profile);
        assertEquals(userId, profile.getUserId());
        verify(userProfileRepository).save(any(com.usermanagement_service.model.UserProfile.class));
    }

    @Test
    void createUserProfile_ProfileAlreadyExists_ThrowsException() {
        // Given
        UserCreationRequest creationRequest = UserCreationRequest.builder()
            .id(userId)
            .alias("NewUser")
            .gender(Gender.NO_INFO)
            .ageGroup(AgeGroup.ADULT)
            .preferences(new HashMap<>())
            .preferredContactMethod(PreferredContactMethod.EMAIL)
            .phoneNr("1234567890")
            .profilePictureUrl("http://example.com/avatar.jpg")
            .build();

        when(authUserRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(testProfile));

        // When & Then
        assertThrows(RuntimeException.class, 
            () -> userManagementService.createUserProfile(creationRequest));
    }

    @Test
    void updateUserProfile_Success() {
        // Given
        UserUpdateRequest updateRequest = new UserUpdateRequest(
            "UpdatedUser",
            Gender.NO_INFO,
            AgeGroup.ADULT,
            Preferences.builder()
                .shareLocation(true)
                .notifyOnDelay(true)
                .autoNotifyContacts(true)
                .checkInInterval(30)
                .enableSOS(true)
                .preferences(new HashMap<>())
                .build(),
            PreferredContactMethod.EMAIL,
            "1234567890",
            java.util.List.of()
        );

        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(testProfile));
        when(userProfileRepository.save(any(com.usermanagement_service.model.UserProfile.class)))
            .thenReturn(testProfile);
        when(authUserRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(emergencyContactRepository.findByRequesterIdAndStatus(userId, RequestStatus.ACCEPTED))
            .thenReturn(java.util.List.of());

        // When
        UserProfileResponse profile = userManagementService.updateUserProfile(userId, updateRequest);

        // Then
        assertNotNull(profile);
        assertEquals(userId, profile.getUserId());
        verify(userProfileRepository).save(any(com.usermanagement_service.model.UserProfile.class));
    }

    @Test
    void updateUserProfile_ProfileNotExists_ThrowsException() {
        // Given
        UserUpdateRequest updateRequest = new UserUpdateRequest(
            "UpdatedUser",
            Gender.NO_INFO,
            AgeGroup.ADULT,
            Preferences.builder()
                .shareLocation(true)
                .notifyOnDelay(true)
                .autoNotifyContacts(true)
                .checkInInterval(30)
                .enableSOS(true)
                .preferences(new HashMap<>())
                .build(),
            PreferredContactMethod.EMAIL,
            "1234567890",
            java.util.List.of()
        );

        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> userManagementService.updateUserProfile(userId, updateRequest));
    }

    @Test
    void addEmergencyContact_Success() {
        // Given
        String contactId = "contact-123";
        com.usermanagement_service.model.UserProfile contactProfile = com.usermanagement_service.model.UserProfile.builder()
            .id("profile-456")
            .userId(contactId)
            .alias("Contact User")
            .gender(Gender.NO_INFO)
            .ageGroup(AgeGroup.ADULT)
            .preferences(null)
            .preferredContactMethod(PreferredContactMethod.EMAIL)
            .phoneNr("1234567890")
            .profilePictureUrl("http://example.com/avatar.jpg")
            .build();

        EmergencyContact emergencyContact = EmergencyContact.builder()
            .id("ec-123")
            .requesterId(userId)
            .contactUserId(contactId)
            .name("Contact User")
            .email("contact@example.com")
            .phone("1234567890")
            .preferredMethod(PreferredContactMethod.EMAIL)
            .status(RequestStatus.PENDING)
            .build();

        when(userProfileRepository.findByUserId(contactId)).thenReturn(Optional.of(contactProfile));
        when(emergencyContactRepository.existsByRequesterIdAndContactUserId(userId, contactId))
            .thenReturn(false);
        when(emergencyContactRepository.save(any(EmergencyContact.class)))
            .thenReturn(emergencyContact);

        // When
        AddEmergencyContactResponse response = userManagementService.addEmergencyContact(userId, contactId);

        // Then
        assertNotNull(response);
        assertEquals(userId, response.getRequesterId());
        assertEquals(contactId, response.getContactUserId());
    }

    @Test
    void addEmergencyContact_ContactAlreadyExists_ThrowsException() {
        // Given
        String contactId = "contact-123";
        com.usermanagement_service.model.UserProfile contactProfile = com.usermanagement_service.model.UserProfile.builder()
            .id("profile-456")
            .userId(contactId)
            .alias("Contact User")
            .gender(Gender.NO_INFO)
            .ageGroup(AgeGroup.ADULT)
            .preferences(null)
            .preferredContactMethod(PreferredContactMethod.EMAIL)
            .phoneNr("1234567890")
            .profilePictureUrl("http://example.com/avatar.jpg")
            .build();

        when(userProfileRepository.findByUserId(contactId)).thenReturn(Optional.of(contactProfile));
        when(emergencyContactRepository.existsByRequesterIdAndContactUserId(userId, contactId))
            .thenReturn(true);

        // When & Then
        assertThrows(RuntimeException.class, () -> userManagementService.addEmergencyContact(userId, contactId));
    }

    @Test
    void addEmergencyContact_ContactUserNotExists_ThrowsException() {
        // Given
        String contactId = "contact-123";
        when(userProfileRepository.findByUserId(contactId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> userManagementService.addEmergencyContact(userId, contactId));
    }
} 