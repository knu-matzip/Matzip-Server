package com.matzip.auth.application;

import com.matzip.user.domain.ProfileBackground;
import com.matzip.user.domain.ProfileImage;
import com.matzip.user.domain.User;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;


@Component
public class ProfileAssignment {

    private final SecureRandom random = new SecureRandom();

    public void assignRandomProfile(User user) {
        ProfileImage[] images = ProfileImage.values();
        ProfileBackground[] backgrounds = ProfileBackground.values();

        // 랜덤 인덱스를 선택하여 배정
        ProfileImage randomImage = images[random.nextInt(images.length)];
        ProfileBackground randomBackground = backgrounds[random.nextInt(backgrounds.length)];

        user.setProfile(randomImage, randomBackground);
    }
}
