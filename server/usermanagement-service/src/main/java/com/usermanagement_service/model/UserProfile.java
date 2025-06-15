package com.usermanagement_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_profiles")
public class UserProfile {
    @Id
    private String id;
    private String userId;  // Reference to the auth user
    private String alias;
    private Gender gender;
    private AgeGroup ageGroup;
    private Preferences preferences;
    private PreferredContactMethod preferredContactMethod;
    private String phoneNr;
    private String profilePictureUrl;
    private List<EmergencyContact> emergencyContacts;
} 