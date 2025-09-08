package com.matzip.auth.api;

import com.matzip.auth.api.dto.KakaoLoginRequest;
import com.matzip.auth.api.dto.LoginResponse;
import com.matzip.auth.api.dto.TokenReissueRequest;
import com.matzip.auth.api.dto.TokenResponse;
import com.matzip.auth.application.AuthService;
import com.matzip.auth.application.dto.ReissueResult;
import com.matzip.common.exception.BusinessException;
import com.matzip.common.exception.code.ErrorCode;
import com.matzip.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

import static org.springframework.http.HttpHeaders.SET_COOKIE;

@RestController
@RequestMapping("/api/vi/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String RT_COOKIE_NAME = "RT";
    private static final String RT_COOKIE_PATH = "/api/v1/auth";

    private final AuthService authService;

    /**
     * 카카오 로그인: 인가 코드 -> 카카오 토큰 교환/유저 조회 -> 우리 서비스 JWT 발급
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody KakaoLoginRequest request) {
        LoginResponse response = authService.login(request);

        ResponseEntity.BodyBuilder builder = ResponseEntity.ok();

        if (StringUtils.hasText(response.getRefreshToken())) {
            // Refresh Token은 바디에 실지 않고 HttpOnly 쿠키로만 내려옴
            ResponseCookie rtCookie = buildRtCookie(response.getRefreshToken(), response.getRefreshTokenExpiresIn());
            builder.header(SET_COOKIE, rtCookie.toString());

            response = LoginResponse.builder()
                    .tokenType(response.getTokenType())
                    .accessToken(response.getAccessToken())
                    .accessTokenExpiresIn(response.getAccessTokenExpiresIn())
                    .userId(response.getUserId())
                    .nickname(response.getNickname())
                    .profileImageUrl(response.getProfileImageUrl())
                    .firstLogin(response.isFirstLogin())
                    .build();
        }

        ApiResponse<LoginResponse> payload = ApiResponse.success(response);
        return builder.body(payload);
    }

    /**
     * 토큰 재발급
     * @param rtCookie
     * @param body
     * @return
     */
    @PostMapping("/token")
    public ResponseEntity<ApiResponse<TokenResponse>> reissue(
            @CookieValue(value = RT_COOKIE_NAME, required = false) String rtCookie,
            @RequestBody(required = false) TokenReissueRequest body
    ) {
        String incomingRt = (body != null && StringUtils.hasText(body.getRefreshToken()))
                ? body.getRefreshToken()
                : rtCookie;

        if (!StringUtils.hasText(incomingRt)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "리프레시 토큰이 없습니다.");
        }

        ReissueResult result = authService.reissue(new TokenReissueRequest(incomingRt));

        // 회전된 새 Refresh Token을 쿠키로 설정
        ResponseCookie rtCookieNew = buildRtCookie(result.refreshToken(), result.refreshTokenExpiresInMs());

        // 바디는 Access Token만
        TokenResponse bodyDto = TokenResponse.builder()
                .tokenType("Bearer")
                .accessToken(result.accessToken())
                .accessTokenExpiresIn(result.accessTokenExpiresInMs())
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, rtCookieNew.toString())
                .body(ApiResponse.success(bodyDto));
    }

    private static ResponseCookie buildRtCookie(String rt, long maxAgeMs) {
        return ResponseCookie.from(RT_COOKIE_NAME, rt)
                .httpOnly(true)      // JS 접근 차단
                .secure(true)        // HTTPS 전용
                .sameSite("Lax")     // 도메인 구조에 따라 None/Strict로 조정
                .path(RT_COOKIE_PATH)
                .maxAge(Duration.ofMillis(maxAgeMs))
                .build();
    }
}
