package com.matzip.auth.application;

import com.matzip.user.domain.ProfileBackground;
import com.matzip.user.domain.ProfileImage;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class ProfileAssignment {

    private final SecureRandom random = new SecureRandom();

    public ProfileImage getRandomProfileImage() {
        ProfileImage[] images = ProfileImage.values();
        return images[random.nextInt(images.length)];
    }

    public ProfileBackground getRandomProfileBackground() {
        ProfileBackground[] backgrounds = ProfileBackground.values();
        return backgrounds[random.nextInt(backgrounds.length)];
    }
}
