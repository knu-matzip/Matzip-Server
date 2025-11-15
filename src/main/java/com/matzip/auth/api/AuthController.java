package com.matzip.auth.api;

import com.matzip.auth.api.dto.KakaoLoginRequest;
import com.matzip.auth.api.dto.LoginResponse;
import com.matzip.auth.api.dto.TokenReissueRequest;
import com.matzip.auth.api.dto.TokenResponse;
import com.matzip.auth.application.AuthService;
import com.matzip.auth.application.util.KakaoAuthorizeUrlBuilder;
import com.matzip.auth.application.dto.ReissueResult;
import com.matzip.auth.application.util.StatelessStateSigner;
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
    private static final String RT_COOKIE_PATH = "/api/v1/auth/";

    private final AuthService authService;
    private final AuthRedirectProperties redirectProperties;

    private final KakaoProperties kakaoProperties;
    private final StatelessStateSigner stateSigner;
    private final KakaoAuthorizeUrlBuilder kakaoAuthorizeUrlBuilder;

    @GetMapping("/authorize")
    public ResponseEntity<Void> authorize(@RequestParam("clientOrigin") String clientOrigin) {

        if (clientOrigin == null || !redirectProperties.getAllowedOrigins().contains(clientOrigin)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "허용되지 않은 Origin입니다.");
        }

        if (!StringUtils.hasText(kakaoProperties.getClientId()) || !StringUtils.hasText(kakaoProperties.getRedirectUri())) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "카카오 OAuth 설정(clientId/redirectUri)이 누락되었습니다.");
        }

        String state = stateSigner.createSignedState(clientOrigin);

        // 카카오 authorize URL 구성
        String authorizeUrl = kakaoAuthorizeUrlBuilder.build(state);

        return ResponseEntity.status(302)
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

    private ResponseCookie buildRtCookie(String rt, long maxAgeMs) {
        return ResponseCookie.from(RT_COOKIE_NAME, rt)
                .httpOnly(true)      // JS 접근 차단
                .secure(redirectProperties.isCookieSecure())        // HTTPS 전용
                .path(RT_COOKIE_PATH)
                .maxAge(Duration.ofMillis(maxAgeMs))
                .build();
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> kakaoCallback(
            @RequestParam("code") @NotBlank String code,
            @RequestParam(name = "state") String state
    ) {
        String origin;
        try {
            // state부터 검증 후 origin 확보
            origin = stateSigner.verifyAndGetOrigin(state);

            if (!redirectProperties.getAllowedOrigins().contains(origin)) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "검증 실패: 허용되지 않은 Origin");
            }
        } catch (Exception e) {
            String defaultFailOrigin = "https://knu-matzip.vercel.app";
            String location = defaultFailOrigin + redirectProperties.getFailurePath() + "?error=" + ErrorCode.UNAUTHORIZED.getCode();

            return ResponseEntity.status(302)
                    .header(HttpHeaders.LOCATION, location)
                    .build();
        }

        try {
            LoginResponse res = authService.login(new KakaoLoginRequest(code));

            // 로그인 성공
            ResponseCookie rtCookie = buildRtCookie(res.getRefreshToken(), res.getRefreshTokenExpiresIn());

            String location = origin + redirectProperties.getSuccessPath();

            return ResponseEntity.status(302)
                    .header(HttpHeaders.SET_COOKIE, rtCookie.toString())
                    .header(HttpHeaders.LOCATION, location)
                    .build();

        } catch (BusinessException e) {
            // 로그인 실패 (State는 정상이었으나, code가 만료되었거나 등등)
            // origin은 신뢰할 수 있으므로, 해당 origin의 실패 페이지로 보냄
            String location = origin + redirectProperties.getFailurePath() + "?error=" + e.getErrorCode().getCode();

            return ResponseEntity.status(302)
                    .header(HttpHeaders.LOCATION, location)
                    .build();
        }
    }

}

