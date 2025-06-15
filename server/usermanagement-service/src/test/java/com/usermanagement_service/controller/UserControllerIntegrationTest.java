package com.usermanagement_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usermanagement_service.dto.*;
import com.usermanagement_service.model.*;
import com.usermanagement_service.repository.UserRepository;
import com.usermanagement_service.repository.EmergencyContactRepository;
import com.usermanagement_service.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmergencyContactRepository emergencyContactRepository;

    @Autowired
    private JwtService jwtService;

    private UUID userId;
    private String validToken;
    private User testUser;
    private UserCreationRequest creationRequest;
    private UserUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        emergencyContactRepository.deleteAll();

        userId = UUID.randomUUID();
        validToken = jwtService.generateToken(userId.toString(), "test@example.com");

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
    void getUserProfile_WhenUserExists_ReturnsUserProfile() throws Exception {
        userRepository.save(testUser);

        mockMvc.perform(get("/api/user")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.alias").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void getUserProfile_WhenUserDoesNotExist_Returns404() throws Exception {
        mockMvc.perform(get("/api/user")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void createUser_WhenUserDoesNotExist_CreatesNewUser() throws Exception {
        mockMvc.perform(post("/api/user")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.alias").value("Test User"));
    }

    @Test
    void createUser_WhenUserExists_Returns409() throws Exception {
        userRepository.save(testUser);

        mockMvc.perform(post("/api/user")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creationRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateUser_WhenUserExists_UpdatesUser() throws Exception {
        userRepository.save(testUser);

        mockMvc.perform(put("/api/user")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alias").value("Updated User"))
                .andExpect(jsonPath("$.gender").value("FEMALE"));
    }

    @Test
    void updateUser_WhenUserDoesNotExist_Returns404() throws Exception {
        mockMvc.perform(put("/api/user")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void addEmergencyContact_WhenContactExists_ReturnsResponse() throws Exception {
        UUID contactId = UUID.randomUUID();
        User contactUser = User.builder()
            .id(contactId)
            .userCode("XYZ789")
            .build();
        userRepository.save(contactUser);

        mockMvc.perform(post("/api/user/emergency-contact")
                .header("Authorization", "Bearer " + validToken)
                .param("contactUserCode", "XYZ789"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requesterId").value(userId.toString()))
                .andExpect(jsonPath("$.contactUserId").value(contactId.toString()));
    }

    @Test
    void addEmergencyContact_WhenContactDoesNotExist_Returns404() throws Exception {
        mockMvc.perform(post("/api/user/emergency-contact")
                .header("Authorization", "Bearer " + validToken)
                .param("contactUserCode", "XYZ789"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPendingEmergencyContacts_WhenRequestsExist_ReturnsList() throws Exception {
        UUID contactId = UUID.randomUUID();
        User contactUser = User.builder()
            .id(contactId)
            .userCode("XYZ789")
            .build();
        userRepository.save(contactUser);

        EmergencyContact contact = EmergencyContact.builder()
            .id(UUID.randomUUID())
            .requesterId(contactId)
            .contactUserId(userId)
            .status(RequestStatus.PENDING)
            .build();
        emergencyContactRepository.save(contact);

        mockMvc.perform(get("/api/user/emergency-contact/pending")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(contactId.toString()))
                .andExpect(jsonPath("$[0].userCode").value("XYZ789"));
    }

    @Test
    void respondToEmergencyContact_WhenRequestExists_UpdatesStatus() throws Exception {
        UUID contactId = UUID.randomUUID();
        User contactUser = User.builder()
            .id(contactId)
            .userCode("XYZ789")
            .build();
        userRepository.save(contactUser);

        EmergencyContact contact = EmergencyContact.builder()
            .id(UUID.randomUUID())
            .requesterId(contactId)
            .contactUserId(userId)
            .status(RequestStatus.PENDING)
            .build();
        emergencyContactRepository.save(contact);

        mockMvc.perform(put("/api/user/emergency-contact/{id}", contact.getId())
                .header("Authorization", "Bearer " + validToken)
                .param("status", "ACCEPTED"))
                .andExpect(status().isOk());

        EmergencyContact updatedContact = emergencyContactRepository.findById(contact.getId()).orElseThrow();
        assertEquals(RequestStatus.ACCEPTED, updatedContact.getStatus());
    }
} 