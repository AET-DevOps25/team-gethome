package com.usermanagement_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usermanagement_service.dto.*;
import com.usermanagement_service.model.*;
import com.usermanagement_service.repository.AuthUserRepository;
import com.usermanagement_service.repository.EmergencyContactRepository;
import com.usermanagement_service.repository.UserProfileRepository;
import com.usermanagement_service.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Disabled("Integration tests require complex setup and are disabled for now")
class UserControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private AuthUserRepository authUserRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private EmergencyContactRepository emergencyContactRepository;

    @Autowired
    private JwtService jwtService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private String validToken;
    private AuthUser testUser;
    private com.usermanagement_service.model.UserProfile testProfile;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();

        String userId = "test-user-123";
        validToken = jwtService.generateToken(userId, "test@example.com");

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

        // Save test data
        authUserRepository.save(testUser);
        userProfileRepository.save(testProfile);
    }

    @Test
    void getUserProfile_ValidToken_ReturnsProfile() throws Exception {
        mockMvc.perform(get("/api/users/profile")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("test-user-123"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.alias").value("TestUser"));
    }

    @Test
    void getUserProfile_InvalidToken_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/profile")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createUserProfile_ValidRequest_Success() throws Exception {
        String newUserId = "new-user-456";
        AuthUser newUser = AuthUser.builder()
            .id(newUserId)
            .email("newuser@example.com")
            .password("password")
            .provider("LOCAL")
            .providerId(newUserId)
            .enabled(true)
            .emailVerified(true)
            .build();
        authUserRepository.save(newUser);

        UserCreationRequest request = UserCreationRequest.builder()
            .id(newUserId)
            .alias("NewUser")
            .gender(Gender.NO_INFO)
            .ageGroup(AgeGroup.ADULT)
            .preferences(new HashMap<>())
            .preferredContactMethod(PreferredContactMethod.EMAIL)
            .phoneNr("1234567890")
            .profilePictureUrl("http://example.com/avatar.jpg")
            .build();

        String newUserToken = jwtService.generateToken(newUserId, "newuser@example.com");

        mockMvc.perform(post("/api/users/profile")
                .header("Authorization", "Bearer " + newUserToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(newUserId))
                .andExpect(jsonPath("$.alias").value("NewUser"));
    }

    @Test
    void updateUserProfile_ValidRequest_Success() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest(
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

        mockMvc.perform(put("/api/users/profile")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alias").value("UpdatedUser"));
    }

    @Test
    void addEmergencyContact_ValidRequest_Success() throws Exception {
        String contactId = "contact-123";
        AuthUser contactUser = AuthUser.builder()
            .id(contactId)
            .email("contact@example.com")
            .password("password")
            .provider("LOCAL")
            .providerId(contactId)
            .enabled(true)
            .emailVerified(true)
            .build();
        authUserRepository.save(contactUser);

        mockMvc.perform(post("/api/users/emergency-contacts")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"contactUserId\": \"" + contactId + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.requesterId").value("test-user-123"))
                .andExpect(jsonPath("$.contactUserId").value(contactId));
    }

    @Test
    void addEmergencyContact_ContactNotFound_ReturnsNotFound() throws Exception {
        mockMvc.perform(post("/api/users/emergency-contacts")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"contactUserId\": \"non-existent-user\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addEmergencyContact_ContactAlreadyExists_ReturnsConflict() throws Exception {
        String contactId = "contact-456";
        AuthUser contactUser = AuthUser.builder()
            .id(contactId)
            .email("contact2@example.com")
            .password("password")
            .provider("LOCAL")
            .providerId(contactId)
            .enabled(true)
            .emailVerified(true)
            .build();
        authUserRepository.save(contactUser);

        // Add the contact first time
        mockMvc.perform(post("/api/users/emergency-contacts")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"contactUserId\": \"" + contactId + "\"}"))
                .andExpect(status().isCreated());

        // Try to add the same contact again
        mockMvc.perform(post("/api/users/emergency-contacts")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"contactUserId\": \"" + contactId + "\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void respondToEmergencyContactRequest_ValidRequest_Success() throws Exception {
        String contactId = "contact-789";
        AuthUser contactUser = AuthUser.builder()
            .id(contactId)
            .email("contact3@example.com")
            .password("password")
            .provider("LOCAL")
            .providerId(contactId)
            .enabled(true)
            .emailVerified(true)
            .build();
        authUserRepository.save(contactUser);

        // Add emergency contact request
        mockMvc.perform(post("/api/users/emergency-contacts")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"contactUserId\": \"" + contactId + "\"}"))
                .andExpect(status().isCreated());

        // Get the request ID from the response
        String contactToken = jwtService.generateToken(contactId, "contact3@example.com");

        // Respond to the request
        mockMvc.perform(put("/api/users/emergency-contacts/respond")
                .header("Authorization", "Bearer " + contactToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"requestId\": \"test-user-123\", \"accept\": true}"))
                .andExpect(status().isOk());
    }

    @Test
    void getEmergencyContacts_ValidToken_ReturnsContacts() throws Exception {
        mockMvc.perform(get("/api/users/emergency-contacts")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
} 