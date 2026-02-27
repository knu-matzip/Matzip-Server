package com.matzip.auth.api;

import com.matzip.auth.api.dto.KakaoLoginRequest;
import com.matzip.auth.api.dto.LoginResponse;
import com.matzip.auth.application.AuthService;
import com.matzip.common.response.ApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RequestMapping("/api/v2/auth")
@RestController
public class AuthControllerV2 {

    private final AuthService authService;

    public AuthControllerV2(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/oauth2")
    public ResponseEntity<ApiResponse<String>> kakaoLogin(@RequestParam("code") String code,
                                                          @RequestParam("redirectUri") String redirectUri) {
        LoginResponse loginResponse = authService.login(new KakaoLoginRequest(code, redirectUri));

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

