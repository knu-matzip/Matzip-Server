package com.matzip.auth.application;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * 최초 로그인 시 랜덤 프로필 이미지를 선택하는 유틸
 */
@Component
public class ProfileImagePicker {

    private static final String[] CANDIDATE_IMAGE_URLS = {
            //Todo 프로필 이미지 URL 넣기
    };

    private final SecureRandom random = new SecureRandom();

    public String pick() {
        int idx = random.nextInt(CANDIDATE_IMAGE_URLS.length);
        return CANDIDATE_IMAGE_URLS[idx];
    }
}
