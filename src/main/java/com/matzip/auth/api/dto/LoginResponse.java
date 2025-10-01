package com.matzip.auth.api.dto;

import lombok.*;

/**
 * 카카오 로그인 완료 후, 우리 서비스의 JWT를 내려주는 응답 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class LoginResponse {

    // 토큰 섹션
    private String tokenType;                 // "Bearer" 고정 사용?
    private String accessToken;
    private long   accessTokenExpiresIn;
    private String refreshToken;
    private long   refreshTokenExpiresIn;

    // 사용자 요약 정보(마이페이지 표시용)
    private Long   userId;
    private String nickname;
    private String profileImageUrl;
    private String profileBackgroundHexCode;

    // 부가 정보
    private boolean firstLogin;               // 최초 로그인 여부(랜덤 닉네임/프로필 부여 확인용)
}
