package com.matzip.auth.dto.response;

import lombok.*;

/**
 * 액세스 토큰 재발급 응답 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TokenResponseDto {

    private String tokenType;
    private String accessToken;
    private long   accessTokenExpiresIn;

}
