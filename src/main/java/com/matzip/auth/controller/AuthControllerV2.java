package com.matzip.auth.controller;

import com.matzip.auth.dto.request.KakaoLoginRequestDto;
import com.matzip.auth.dto.response.LoginResponseDto;
import com.matzip.auth.service.AuthService;
import com.matzip.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@Tag(name = "인증", description = "카카오 로그인 및 JWT 토큰 발급 API")
@RequestMapping("/api/v2/auth")
@RestController
public class AuthControllerV2 {

    private final AuthService authService;

    public AuthControllerV2(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "카카오 로그인 (v2)", description = "인가 코드와 redirectUri로 로그인하고, 액세스 토큰을 본문으로, 리프레시 토큰을 쿠키로 반환한다.")
    @GetMapping("/oauth2")
    public ResponseEntity<ApiResponse<String>> kakaoLogin(@RequestParam("code") String code,
                                                          @RequestParam("redirectUri") String redirectUri) {
        LoginResponseDto loginResponse = authService.login(new KakaoLoginRequestDto(code, redirectUri));

//        ResponseCookie accessTokenCookie =
//                generateCookie("accessToken", loginResponse.getAccessToken(), Duration.ofHours(1));
        ResponseCookie refreshTokenCookie =
                generateCookie("refreshToken", loginResponse.getRefreshToken(), Duration.ofDays(14));

        return ResponseEntity.ok()
//                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(ApiResponse.success(loginResponse.getAccessToken()));
    }

    private ResponseCookie generateCookie(String name, String value, Duration expires) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .path("/")
                .maxAge(expires)
                .build();
    }
}
