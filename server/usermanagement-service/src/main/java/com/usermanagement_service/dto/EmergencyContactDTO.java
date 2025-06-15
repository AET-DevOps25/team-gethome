package com.usermanagement_service.dto;

import com.usermanagement_service.model.PreferredContactMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyContactDTO {
    private String name;
    private String email;
    private String phone;
    private PreferredContactMethod preferredMethod;
} 