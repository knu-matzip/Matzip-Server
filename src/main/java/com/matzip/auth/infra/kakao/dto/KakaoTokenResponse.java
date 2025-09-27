package com.matzip.auth.infra.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카카오 토큰 교환 응답 바인딩용 DTO
 * 외부 스펙 바인딩만 담당(내부 API DTO와 분리)
 */
@Getter
@NoArgsConstructor
public class KakaoTokenResponse {

    // 토큰 타입, bearer로 고정
    @JsonProperty("token_type")
    private String tokenType;

    // 사용자 액세스 토큰 값
    @JsonProperty("access_token")
    private String accessToken;

    // 액세스 토큰 만료 시간(초)
    @JsonProperty("expires_in")
    private Long expiresIn;

    // 사용자 리프레시 토큰 값
    @JsonProperty("refresh_token")
    private String refreshToken;

    // 리프레시 토큰 만료 시간(초)
    @JsonProperty("refresh_token_expires_in")
    private Long refreshTokenExpiresIn;

}
