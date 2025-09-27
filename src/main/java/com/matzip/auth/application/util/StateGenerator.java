package com.matzip.auth.application.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

// CSRF 방지를 위해 예측 불가능한 URL-safe 토큰을 생성
@Component
public class StateGenerator {

    private static final int STATE_BYTES = 32; // 256bit
    private final SecureRandom random = new SecureRandom();

    public String generate() {
        byte[] buf = new byte[STATE_BYTES];
        random.nextBytes(buf);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }
}
