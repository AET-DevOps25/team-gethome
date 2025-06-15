package com.usermanagement_service.service;

import org.springframework.stereotype.Component;
import java.util.Random;

@Component
public class ProfilePictureGenerator {
    private static final String[] AVATAR_STYLES = {
        "adventurer", "avataaars", "bottts", "croodles", "fun-emoji",
        "micah", "miniavs", "pixel-art", "personas", "thumbs"
    };
    private final Random random = new Random();

    public String generate() {
        String style = AVATAR_STYLES[random.nextInt(AVATAR_STYLES.length)];
        return String.format("https://api.dicebear.com/7.x/%s/svg", style);
    }
} 