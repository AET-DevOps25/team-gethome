package com.usermanagement_service.dto;

import com.usermanagement_service.model.AgeGroup;
import com.usermanagement_service.model.Gender;
import com.usermanagement_service.model.PreferredContactMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private String id;
    private String userId;
    private String email;
    private String alias;
    private Gender gender;
    private AgeGroup ageGroup;
    private Map<String, Object> preferences;
    private PreferredContactMethod preferredContactMethod;
    private String phoneNr;
    private String profilePictureUrl;
    private List<EmergencyContactDTO> emergencyContacts;
} 