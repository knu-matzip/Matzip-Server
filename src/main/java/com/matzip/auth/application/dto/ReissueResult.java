package com.matzip.auth.application.dto;

public record ReissueResult(
        String accessToken,
        long accessTokenExpiresInMs,
        String refreshToken,             // 쿠키로 내려줄 RT
        long refreshTokenExpiresInMs     // 쿠키 Max-Age 계산용
) {}
