package com.matzip.auth.api;

import com.matzip.auth.api.dto.KakaoLoginRequest;
import com.matzip.auth.api.dto.LoginResponse;
import com.matzip.auth.api.dto.TokenReissueRequest;
import com.matzip.auth.api.dto.TokenResponse;
import com.matzip.auth.application.AuthService;
import com.matzip.auth.application.util.KakaoAuthorizeUrlBuilder;
import com.matzip.auth.application.util.StateGenerator;
import com.matzip.auth.application.dto.ReissueResult;
import com.matzip.common.config.AuthRedirectProperties;
import com.matzip.common.config.KakaoProperties;
import com.matzip.common.exception.BusinessException;
import com.matzip.common.exception.code.ErrorCode;
import com.matzip.common.response.ApiResponse;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;


@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String RT_COOKIE_NAME = "RT";
    private static final String STATE_COOKIE_NAME = "STATE";
    private static final String RT_COOKIE_PATH = "/api/v1/auth";

    private final AuthService authService;
    private final AuthRedirectProperties redirectProperties;

    private final KakaoProperties kakaoProperties;
    private final StateGenerator stateGenerator;
    private KakaoAuthorizeUrlBuilder kakaoAuthorizeUrlBuilder;

    @GetMapping("/authorize")
    public ResponseEntity<Void> authorize() {
        if (!StringUtils.hasText(kakaoProperties.getClientId()) || !StringUtils.hasText(kakaoProperties.getRedirectUri())) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "카카오 OAuth 설정(clientId/redirectUri)이 누락되었습니다.");
        }

        // 1) state 생성 후 HttpOnly 쿠키로 저장
        String state = stateGenerator.generate();
        ResponseCookie stateCookie = ResponseCookie.from(STATE_COOKIE_NAME, state)
                .httpOnly(true)
                .secure(redirectProperties.isCookieSecure())
                .sameSite(redirectProperties.getCookieSameSite())
                .path(RT_COOKIE_PATH)
                .maxAge(Duration.ofMinutes(5))
                .build();

        // 2) 카카오 authorize URL 구성
        String authorizeUrl = kakaoAuthorizeUrlBuilder.build(state);

        return ResponseEntity.status(302)
                .header(HttpHeaders.SET_COOKIE, stateCookie.toString())
                .header(HttpHeaders.LOCATION, authorizeUrl)
                .build();
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

    @GetMapping("/callback")
    public ResponseEntity<Void> kakaoCallback(
            @RequestParam("code") @NotBlank String code,
            @RequestParam(name = "state", required = false) String state
    ) {
        // 1) 서버에서 로그인 실행 (code -> 토큰교환/유저조회 -> JWT 발급)
        LoginResponse res = authService.login(new KakaoLoginRequest(code));

        // 2) Refresh Token을 HttpOnly 쿠키로 설정 (Acess Token은 바디/URL에 싣지 않음)
        ResponseCookie rtCookie = ResponseCookie.from(RT_COOKIE_NAME, res.getRefreshToken())
                .httpOnly(true)
                .secure(redirectProperties.isCookieSecure())     // 로컬은 false, 운영은 true
                .sameSite(redirectProperties.getCookieSameSite())// "Lax" 또는 "None"
                .path(RT_COOKIE_PATH)
                .maxAge(Duration.ofMillis(res.getRefreshTokenExpiresIn()))
                .build();

        // 3) 프론트 성공 URL로 302 리다이렉트 (필요 시 state를 그대로 붙여 전달)
        String location = redirectProperties.getSuccessUri();
        if (state != null && !state.isBlank()) {
            location = location + (location.contains("?") ? "&" : "?") + "state=" + state;
        }

        return ResponseEntity.status(302)
                .header("Set-Cookie", rtCookie.toString())
                .header("Location", location)
                .build();
    }
}
