package com.usermanagement_service.service;

import com.usermanagement_service.dto.*;
import com.usermanagement_service.model.*;
import com.usermanagement_service.repository.UserRepository;
import com.usermanagement_service.repository.EmergencyContactRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmergencyContactRepository emergencyContactRepository;

    @Mock
    private UserCodeGenerator userCodeGenerator;

    @Mock
    private ProfilePictureGenerator profilePictureGenerator;

    private UserManagementService userManagementService;

    private UUID userId;
    private User testUser;
    private UserCreationRequest creationRequest;
    private UserUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        userManagementService = new UserManagementService(userRepository, emergencyContactRepository, userCodeGenerator, profilePictureGenerator);
        
        userId = UUID.randomUUID();
        testUser = User.builder()
            .id(userId)
            .email("test@example.com")
            .alias("Test User")
            .userCode("ABC123")
            .gender(Gender.MALE)
            .ageGroup(AgeGroup.ADULT)
            .preferences(new Preferences(AiTone.FRIENDLY, Talkativeness.TALKATIVE, SocialDistance.INTERESTED, List.of("sports")))
            .preferredContactMethod(PreferredContactMethod.EMAIL)
            .build();

        creationRequest = new UserCreationRequest(
            "Test User",
            Gender.MALE,
            AgeGroup.ADULT,
            new Preferences(AiTone.FRIENDLY, Talkativeness.TALKATIVE, SocialDistance.INTERESTED, List.of("sports")),
            PreferredContactMethod.EMAIL,
            null
        );

        updateRequest = new UserUpdateRequest(
            "Updated User",
            Gender.FEMALE,
            AgeGroup.YOUNG_ADULT,
            new Preferences(AiTone.NEUTRAL, Talkativeness.LISTENING, SocialDistance.DISTANT, List.of("music")),
            PreferredContactMethod.SMS,
            "+1234567890"
        );
    }

    @Test
    void getUserProfile_WhenUserExists_ReturnsUserProfile() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        UserProfile profile = userManagementService.getUserProfile(userId);

        assertNotNull(profile);
        assertEquals(userId, profile.id());
        assertEquals("Test User", profile.alias());
        assertEquals("test@example.com", profile.email());
    }

    @Test
    void getUserProfile_WhenUserDoesNotExist_ThrowsException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userManagementService.getUserProfile(userId));
    }

    @Test
    void createUser_WhenUserDoesNotExist_CreatesNewUser() {
        when(userRepository.existsById(userId)).thenReturn(false);
        when(userCodeGenerator.generate()).thenReturn("ABC123");
        when(profilePictureGenerator.generate()).thenReturn("https://example.com/avatar.png");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserProfile profile = userManagementService.createUser(userId, "test@example.com", creationRequest);

        assertNotNull(profile);
        assertEquals(userId, profile.id());
        assertEquals("Test User", profile.alias());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_WhenUserExists_ThrowsException() {
        when(userRepository.existsById(userId)).thenReturn(true);

        assertThrows(RuntimeException.class, 
            () -> userManagementService.createUser(userId, "test@example.com", creationRequest));
    }

    @Test
    void updateUser_WhenUserExists_UpdatesUser() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserProfile profile = userManagementService.updateUser(userId, updateRequest);

        assertNotNull(profile);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_WhenUserDoesNotExist_ThrowsException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userManagementService.updateUser(userId, updateRequest));
    }

    @Test
    void addEmergencyContact_WhenContactExists_ReturnsResponse() {
        UUID contactId = UUID.randomUUID();
        User contactUser = User.builder()
            .id(contactId)
            .userCode("XYZ789")
            .build();

        when(userRepository.findByUserCode("XYZ789")).thenReturn(Optional.of(contactUser));
        when(emergencyContactRepository.existsByRequesterIdAndContactUserId(userId, contactId))
            .thenReturn(false);
        when(emergencyContactRepository.save(any(EmergencyContact.class)))
            .thenAnswer(i -> i.getArgument(0));

        AddEmergencyContactResponse response = userManagementService.addEmergencyContact(userId, "XYZ789");

        assertNotNull(response);
        assertEquals(userId, response.requesterId());
        assertEquals(contactId, response.contactUserId());
        verify(emergencyContactRepository).save(any(EmergencyContact.class));
    }

    @Test
    void addEmergencyContact_WhenContactDoesNotExist_ThrowsException() {
        when(userRepository.findByUserCode("XYZ789")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userManagementService.addEmergencyContact(userId, "XYZ789"));
    }

    @Test
    void addEmergencyContact_WhenRequestAlreadyExists_ThrowsException() {
        UUID contactId = UUID.randomUUID();
        User contactUser = User.builder()
            .id(contactId)
            .userCode("XYZ789")
            .build();

        when(userRepository.findByUserCode("XYZ789")).thenReturn(Optional.of(contactUser));
        when(emergencyContactRepository.existsByRequesterIdAndContactUserId(userId, contactId))
            .thenReturn(true);

        assertThrows(RuntimeException.class, () -> userManagementService.addEmergencyContact(userId, "XYZ789"));
    }
} 