package com.usermanagement_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Preferences {
    private boolean shareLocation; // Whether to share real-time location
    private boolean notifyOnDelay; // Whether to notify contacts on delays
    private boolean autoNotifyContacts; // Whether to automatically notify contacts
    private int checkInInterval; // How often to check in (in minutes)
    private boolean enableSOS; // Whether to enable SOS feature
    private Map<String, Object> preferences; // Additional preferences
} 