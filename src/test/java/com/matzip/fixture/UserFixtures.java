package com.matzip.fixture;

import com.matzip.user.domain.ProfileBackground;
import com.matzip.user.domain.ProfileImage;
import com.matzip.user.domain.User;

import java.util.Random;

public class UserFixtures {

    private UserFixtures() {
    }

    public static User createUserWith(String identifier) {
        return User.builder()
                .kakaoId(new Random(1000000).nextLong())
                .nickname(identifier)
                .profileImage(ProfileImage.CAT)
                .profileBackground(ProfileBackground.COLOR_01)
                .build();
    }
}
