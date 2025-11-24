package com.matzip.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * 카카오 인가코드 기반 로그인 요청 DTO
 * 클라이언트가 카카오 로그인 후 redirectUri에 전달된 code만 서버로 전달
 * 서버는 이 code를 사용해 카카오 API와 토큰 교환을 수행
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class KakaoLoginRequest {

    @NotBlank(message = "인가 코드는 필수입니다.")
    private String code;

    private String redirectUri;
}
