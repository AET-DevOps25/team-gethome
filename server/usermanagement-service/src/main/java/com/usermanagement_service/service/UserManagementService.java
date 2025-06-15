package com.usermanagement_service.service;

import com.usermanagement_service.dto.*;
import com.usermanagement_service.model.*;
import com.usermanagement_service.repository.EmergencyContactRepository;
import com.usermanagement_service.repository.UserProfileRepository;
import com.usermanagement_service.repository.AuthUserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserManagementService {
    private final EmergencyContactRepository emergencyContactRepository;
    private final UserProfileRepository userProfileRepository;
    private final AuthUserRepository authUserRepository;
    private final UserCodeGenerator userCodeGenerator;
    private final ProfilePictureGenerator profilePictureGenerator;
    private static final Logger logger = LoggerFactory.getLogger(UserManagementService.class);

    public UserProfileResponse getUserProfile(String userId) {
        logger.info("Getting user profile for userId: {}", userId);
        
        // First check if user exists in auth service
        AuthUser authUser = authUserRepository.findById(userId)
            .orElseThrow(() -> {
                logger.error("User not found in auth service for userId: {}", userId);
                return new RuntimeException("User not found in auth service");
            });
            
        // Then check if profile exists
        com.usermanagement_service.model.UserProfile profile = userProfileRepository.findByUserId(userId)
            .orElse(null);
            
        // If no profile exists, return just the auth user data
        if (profile == null) {
            logger.info("No profile found for userId: {}, returning auth user data only", userId);
            return UserProfileResponse.builder()
                .userId(authUser.getId())
                .email(authUser.getEmail())
                .emergencyContacts(List.of()) // Return empty list for new profiles
                .build();
        }
        
        // Get emergency contacts
        List<EmergencyContact> emergencyContacts = emergencyContactRepository.findByRequesterIdAndStatus(userId, RequestStatus.ACCEPTED);
        List<EmergencyContactDTO> emergencyContactDTOs = emergencyContacts.stream()
            .map(contact -> EmergencyContactDTO.builder()
                .name(contact.getName())
                .email(contact.getEmail())
                .phone(contact.getPhone())
                .preferredMethod(contact.getPreferredMethod())
                .build())
            .collect(Collectors.toList());
        
        // If profile exists, merge the data
        return UserProfileResponse.builder()
            .id(profile.getId())
            .userId(profile.getUserId())
            .email(authUser.getEmail())
            .alias(profile.getAlias())
            .gender(profile.getGender())
            .ageGroup(profile.getAgeGroup())
            .preferences(profile.getPreferences() != null ? profile.getPreferences().getPreferences() : null)
            .preferredContactMethod(profile.getPreferredContactMethod())
            .phoneNr(profile.getPhoneNr())
            .profilePictureUrl(profile.getProfilePictureUrl())
            .emergencyContacts(emergencyContactDTOs)
            .build();
    }

    public UserProfileResponse createUserProfile(UserCreationRequest request) {
        logger.info("Creating user profile with request: {}", request);
        
        // First verify user exists in auth service
        AuthUser authUser = authUserRepository.findById(request.getId())
            .orElseThrow(() -> new RuntimeException("User not found in auth service"));
        
        // Check if profile already exists
        if (userProfileRepository.findByUserId(request.getId()).isPresent()) {
            throw new RuntimeException("Profile already exists for this user");
        }

        Map<String, Object> reqPrefs = request.getPreferences();
        Preferences preferences = Preferences.builder()
            .shareLocation(Boolean.TRUE.equals(reqPrefs.get("shareLocation")))
            .notifyOnDelay(Boolean.TRUE.equals(reqPrefs.get("notifyOnDelay")))
            .autoNotifyContacts(Boolean.TRUE.equals(reqPrefs.get("autoNotifyContacts")))
            .checkInInterval(reqPrefs.get("checkInInterval") != null ? ((Number) reqPrefs.get("checkInInterval")).intValue() : 30)
            .enableSOS(Boolean.TRUE.equals(reqPrefs.get("enableSOS")))
            .preferences(reqPrefs)
            .build();

        com.usermanagement_service.model.UserProfile profile = com.usermanagement_service.model.UserProfile.builder()
            .userId(request.getId())
            .alias(request.getAlias())
            .gender(request.getGender())
            .ageGroup(request.getAgeGroup())
            .preferences(preferences)
            .preferredContactMethod(request.getPreferredContactMethod())
            .phoneNr(request.getPhoneNr())
            .profilePictureUrl(request.getProfilePictureUrl())
            .build();
            
        profile = userProfileRepository.save(profile);
        logger.info("User profile created successfully for userId: {}", request.getId());
        
        return UserProfileResponse.builder()
            .id(profile.getId())
            .userId(profile.getUserId())
            .email(authUser.getEmail())
            .alias(profile.getAlias())
            .gender(profile.getGender())
            .ageGroup(profile.getAgeGroup())
            .preferences(profile.getPreferences() != null ? profile.getPreferences().getPreferences() : null)
            .preferredContactMethod(profile.getPreferredContactMethod())
            .phoneNr(profile.getPhoneNr())
            .profilePictureUrl(profile.getProfilePictureUrl())
            .emergencyContacts(List.of()) // Return empty list for new profiles
            .build();
    }

    @Transactional
    public UserProfileResponse updateUserProfile(String userId, UserUpdateRequest request) {
        logger.info("Updating user profile for userId: {}", userId);
        com.usermanagement_service.model.UserProfile profile = userProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("User profile not found"));

        // Update basic profile information
        profile.setAlias(request.alias());
        profile.setGender(request.gender());
        profile.setAgeGroup(request.ageGroup());
        profile.setPreferredContactMethod(request.preferredContactMethod());
        profile.setPhoneNr(request.phoneNr());

        // Update preferences
        Preferences reqPrefs = request.preferences();
        Preferences preferences = Preferences.builder()
            .shareLocation(reqPrefs.isShareLocation())
            .notifyOnDelay(reqPrefs.isNotifyOnDelay())
            .autoNotifyContacts(reqPrefs.isAutoNotifyContacts())
            .checkInInterval(reqPrefs.getCheckInInterval())
            .enableSOS(reqPrefs.isEnableSOS())
            .preferences(reqPrefs.getPreferences())
            .build();
        profile.setPreferences(preferences);

        // Handle emergency contacts
        if (request.emergencyContacts() != null && !request.emergencyContacts().isEmpty()) {
            // Delete existing emergency contacts
            emergencyContactRepository.deleteByRequesterId(userId);

            // Create and save new emergency contacts
            List<EmergencyContact> emergencyContacts = request.emergencyContacts().stream()
                .map(dto -> EmergencyContact.builder()
                    .requesterId(userId)
                    .contactUserId(dto.getEmail())
                    .name(dto.getName())
                    .email(dto.getEmail())
                    .phone(dto.getPhone())
                    .preferredMethod(dto.getPreferredMethod())
                    .status(RequestStatus.ACCEPTED)
                    .build())
                .collect(Collectors.toList());

            emergencyContacts = emergencyContactRepository.saveAll(emergencyContacts);
            logger.info("Saved {} emergency contacts for user {}", emergencyContacts.size(), userId);
        }

        // Save the updated profile
        profile = userProfileRepository.save(profile);
        logger.info("User profile updated successfully for userId: {}", userId);

        // Get the updated emergency contacts
        List<EmergencyContact> updatedEmergencyContacts = emergencyContactRepository.findByRequesterIdAndStatus(userId, RequestStatus.ACCEPTED);
        List<EmergencyContactDTO> updatedEmergencyContactDTOs = updatedEmergencyContacts.stream()
            .map(contact -> EmergencyContactDTO.builder()
                .name(contact.getName())
                .email(contact.getEmail())
                .phone(contact.getPhone())
                .preferredMethod(contact.getPreferredMethod())
                .build())
            .collect(Collectors.toList());

        // Build the response
        return UserProfileResponse.builder()
            .id(profile.getId())
            .userId(profile.getUserId())
            .alias(profile.getAlias())
            .gender(profile.getGender())
            .ageGroup(profile.getAgeGroup())
            .preferences(profile.getPreferences() != null ? profile.getPreferences().getPreferences() : null)
            .preferredContactMethod(profile.getPreferredContactMethod())
            .phoneNr(profile.getPhoneNr())
            .profilePictureUrl(profile.getProfilePictureUrl())
            .emergencyContacts(updatedEmergencyContactDTOs)
            .build();
    }

    public AddEmergencyContactResponse addEmergencyContact(String requesterId, String contactUserId) {
        com.usermanagement_service.model.UserProfile contactProfile = userProfileRepository.findByUserId(contactUserId)
            .orElseThrow(() -> new RuntimeException("Contact user not found"));

        if (emergencyContactRepository.existsByRequesterIdAndContactUserId(requesterId, contactProfile.getUserId())) {
            throw new RuntimeException("Emergency contact already exists");
        }

        EmergencyContact contact = EmergencyContact.builder()
            .requesterId(requesterId)
            .contactUserId(contactProfile.getUserId())
            .status(RequestStatus.PENDING)
            .build();

        contact = emergencyContactRepository.save(contact);
        return new AddEmergencyContactResponse(contact.getId(), contact.getRequesterId(), contact.getContactUserId());
    }

    public List<EmergencyContact> getPendingEmergencyContactRequests(String userId) {
        return emergencyContactRepository.findByContactUserIdAndStatus(userId, RequestStatus.PENDING);
    }

    public void respondToEmergencyContactRequest(String requestId, boolean accept) {
        EmergencyContact contact = emergencyContactRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Emergency contact request not found"));

        contact.setStatus(accept ? RequestStatus.ACCEPTED : RequestStatus.DENIED);
        emergencyContactRepository.save(contact);
    }

    public List<EmergencyContact> getEmergencyContacts(String userId) {
        return emergencyContactRepository.findByRequesterIdAndStatus(userId, RequestStatus.ACCEPTED);
    }

    public List<EmergencyContact> getEmergencyContactsOf(String userId) {
        return emergencyContactRepository.findByContactUserIdAndStatus(userId, RequestStatus.ACCEPTED);
    }

    public List<EmergencyContact> getEmergencyContactsFor(String userId) {
        return emergencyContactRepository.findByRequesterIdAndStatus(userId, RequestStatus.ACCEPTED);
    }

    public List<EmergencyContact> getEmergencyContactsOfFor(String userId) {
        return emergencyContactRepository.findByContactUserIdAndStatus(userId, RequestStatus.ACCEPTED);
    }

    public void removeEmergencyContact(String userId, String contactId) {
        emergencyContactRepository.deleteByRequesterIdAndContactUserId(userId, contactId);
    }

    public void removeEmergencyContactOf(String requesterId, String userId) {
        emergencyContactRepository.deleteByRequesterIdAndContactUserId(requesterId, userId);
    }
} 