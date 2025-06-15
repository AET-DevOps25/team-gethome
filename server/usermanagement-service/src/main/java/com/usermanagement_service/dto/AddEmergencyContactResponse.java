package com.usermanagement_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddEmergencyContactResponse {
    private String id;
    private String requesterId;
    private String contactUserId;
}