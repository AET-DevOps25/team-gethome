package com.usermanagement_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "emergency_contacts")
public class EmergencyContact {
    @Id
    private String id;
    private String requesterId;
    private String contactUserId;
    private String name;
    private String email;
    private String phone;
    private PreferredContactMethod preferredMethod;
    private RequestStatus status;
} 