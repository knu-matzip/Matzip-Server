package com.matzip.auth.infra.kakao;

import com.matzip.auth.infra.kakao.dto.KakaoTokenResponse;
import com.matzip.auth.infra.kakao.dto.KakaoUserResponse;
import com.matzip.common.config.KakaoProperties;
import com.matzip.common.exception.BusinessException;
import com.matzip.common.exception.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * 카카오 REST API 호출 클라이언트
 */
@Component
@RequiredArgsConstructor
public class KakaoApiClient {

    private static final String TOKEN_HOST = "https://kauth.kakao.com";
    private static final String API_HOST   = "https://kapi.kakao.com";

    private final WebClient.Builder webClientBuilder;
    private final KakaoProperties kakaoProperties;

    /**
     * 인가코드 - 카카오 액세스 토큰 교환
     */
    public KakaoTokenResponse exchangeToken(String authorizationCode) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", kakaoProperties.getClientId());
        form.add("redirect_uri", kakaoProperties.getRedirectUri());
        form.add("code", authorizationCode);


        try {
            return webClientBuilder
                    .baseUrl(TOKEN_HOST)
                    .build()
                    .post()
                    .uri("/oauth/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(form)
                    .retrieve()
                    .onStatus(
                            // 4xx/5xx를 공통 비즈니스 예외로 래핑
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            resp -> resp.bodyToMono(String.class)
                                    .defaultIfEmpty("Kakao token exchange failed")
                                    .flatMap(body -> Mono.error(new BusinessException(
                                            ErrorCode.KAKAO_LOGIN_FAILED,
                                            "카카오 토큰 교환 실패: " + body)))
                    )
                    .bodyToMono(KakaoTokenResponse.class)
                    .block();
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            // 네트워크/타임아웃 등 비정상 예외를 서비스 공통 에러 코드로 변환
            throw new BusinessException(ErrorCode.KAKAO_API_ERROR, "카카오 토큰 교환 중 오류", e);
        }
    }

    /**
     * 카카오 사용자 정보 조회
     * Authorization 헤더: Bearer {accessToken}
     */
    public KakaoUserResponse getUser(String accessToken) {
        try {
            return webClientBuilder
                    .baseUrl(API_HOST)
                    .build()
                    .get()
                    .uri("/v2/user/me")
                    .accept(MediaType.APPLICATION_JSON)
                    .headers(h -> h.setBearerAuth(accessToken))
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            resp -> resp.bodyToMono(String.class)
                                    .defaultIfEmpty("Kakao get user failed")
                                    .flatMap(body -> Mono.error(new BusinessException(
                                            ErrorCode.KAKAO_LOGIN_FAILED,
                                            "카카오 사용자 조회 실패: " + body)))
                    )
                    .bodyToMono(KakaoUserResponse.class)
                    .block();
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.KAKAO_API_ERROR, "카카오 사용자 정보 조회 중 오류", e);
        }
    }
}
