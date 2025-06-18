package com.message_service.client;

import com.message_service.config.UserManagementProperties;
import com.message_service.dto.UserProfileMinimalDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UserMgmtClient {
    private final RestTemplate restTemplate;
    private final UserManagementProperties userManagementProperties;

    @Autowired
    public UserMgmtClient(RestTemplate restTemplate, UserManagementProperties userManagementProperties) {
        this.restTemplate = restTemplate;
        this.userManagementProperties = userManagementProperties;
    }

    public UserProfileMinimalDTO getUserById(String userId, String authHeader) {
        String url = userManagementProperties.getUrl() + "/api/users/" + userId + "/profile";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<UserProfileMinimalDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                UserProfileMinimalDTO.class
        );
        return response.getBody();
    }
}
