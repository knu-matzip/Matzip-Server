package com.matzip.auth.application.util;

import com.matzip.common.config.AuthRedirectProperties;
import com.matzip.common.exception.BusinessException;
import com.matzip.common.exception.code.ErrorCode;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Objects;

@Component
public class StatelessStateSigner {

    private final Mac hmac;
    private final String HMAC_ALG = "HmacSHA256";

    public StatelessStateSigner(AuthRedirectProperties properties) {
        try {
            String secret = properties.getStateSecret();
            if (secret == null || secret.isBlank()) {
                throw new IllegalStateException("State-Secret이 설정되지 않았습니다.");
            }
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALG);
            this.hmac = Mac.getInstance(HMAC_ALG);
            this.hmac.init(secretKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("State 서명기(HMAC) 초기화 실패", e);
        }
    }

    /**
     * Origin을 서명하여 state 문자열 생성
     */
    public String createSignedState(String origin) {
        String encodedOrigin = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(origin.getBytes(StandardCharsets.UTF_8));
        String signature;
        synchronized (hmac) { // Mac은 thread-safe하지 않음
            byte[] signatureBytes = hmac.doFinal(encodedOrigin.getBytes(StandardCharsets.UTF_8));
            signature = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(signatureBytes);
        }
        return encodedOrigin + "." + signature;
    }

    /**
     * state 문자열을 검증하고 Origin 복원
     */
    public String verifyAndGetOrigin(String state) {
        try {
            String[] parts = state.split("\\.");
            if (parts.length != 2) throw new IllegalArgumentException("Invalid state format");

            String encodedOrigin = parts[0];
            String signature = parts[1];

            String expectedSignature;
            synchronized (hmac) {
                byte[] signatureBytes = hmac.doFinal(encodedOrigin.getBytes(StandardCharsets.UTF_8));
                expectedSignature = Base64.getUrlEncoder().encodeToString(signatureBytes);
            }

            if (!Objects.equals(signature, expectedSignature)) {
                throw new SecurityException("Invalid state signature");
            }

            return new String(Base64.getUrlDecoder().decode(encodedOrigin), StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "유효하지 않은 State입니다.", e);
        }
    }
}
