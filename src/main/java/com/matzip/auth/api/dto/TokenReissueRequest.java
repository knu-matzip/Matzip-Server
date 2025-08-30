package com.matzip.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * 액세스 토큰 재발급 요청 DTO
 * 클라이언트는 저장 중인 refreshToken을 전달
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TokenReissueRequest {

    @NotBlank(message = "리프레시 토큰은 필수입니다.")
    private String refreshToken;
}
