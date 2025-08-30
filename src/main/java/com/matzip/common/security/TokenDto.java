package com.matzip.common.security;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenDto {
    private String accessToken;
    private String refreshToken;
}
