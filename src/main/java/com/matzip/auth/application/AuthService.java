package com.matzip.auth.application;

import com.matzip.auth.api.dto.KakaoLoginRequest;
import com.matzip.auth.api.dto.LoginResponse;
import com.matzip.auth.api.dto.TokenReissueRequest;
import com.matzip.auth.application.dto.ReissueResult;
import com.matzip.auth.domain.RefreshToken;
import com.matzip.auth.infra.kakao.KakaoLoginApiClient;
import com.matzip.auth.infra.kakao.dto.KakaoTokenResponse;
import com.matzip.auth.infra.kakao.dto.KakaoUserResponse;
import com.matzip.common.config.JwtProperties;
import com.matzip.common.exception.BusinessException;
import com.matzip.common.exception.code.ErrorCode;
import com.matzip.common.security.jwt.JwtProvider;
import com.matzip.common.security.jwt.RefreshTokenRepository;
import com.matzip.user.domain.User;
import com.matzip.user.infra.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final KakaoLoginApiClient kakaoLoginApiClient;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProps;

    private final NickNameGenerator nickNameGenerator;
    private final ProfileAssignment profileAssignment;

    private long accessTokenTtlMs() {
        return jwtProps.getExpirationTime();
    }
    private long refreshTokenTtlMs() {
        return jwtProps.getRefreshExpirationTime();
    }

    @Transactional
    public LoginResponse login(KakaoLoginRequest loginRequest) {
        // 1) 인가 코드 -> 카카오 액세스 토큰 교환
        KakaoTokenResponse kakoToken = kakaoLoginApiClient.exchangeToken(loginRequest.getCode());
        if (kakoToken == null || kakoToken.getAccessToken() == null) {
            throw new BusinessException(ErrorCode.KAKAO_LOGIN_FAILED, "카카오 토큰 응답이 비어 있습니다.");
        }

        // 2) 카카오 사용자 정보 조회
        KakaoUserResponse kakaoUser = kakaoLoginApiClient.getUser(kakoToken.getAccessToken());
        if (kakaoUser == null || kakaoUser.getId() == null) {
            throw new BusinessException(ErrorCode.KAKAO_LOGIN_FAILED, "카카오 사용자 정보 조회 실패");
        }

        // 3) 우리 서비스 사용자 조회/생성
        boolean firstLogin = false;
        User user = userRepository.findByKakaoId(kakaoUser.getId()).orElse(null);

        if (user == null) {
            String nickname = nickNameGenerator.generate();

            user = User.builder()
                    .kakaoId(kakaoUser.getId())
                    .nickname(nickname)
                    .build();

            profileAssignment.assignRandomProfile(user);

            user = userRepository.save(user);
            firstLogin = true;
        }

        // 4) 자체 JWT 발급
        String accessToken = jwtProvider.createAccessToken(user.getId());
        String refreshToken = jwtProvider.createRefreshToken(user.getId());

        // 4-1) RefreshToken 저장/갱신
        RefreshToken saved = refreshTokenRepository.findByUserId(user.getId()).orElse(null);
        if (saved == null) {
            saved = RefreshToken.builder()
                    .userId(user.getId())
                    .token(refreshToken)
                    .build();
            refreshTokenRepository.save(saved);
        } else {
            saved.updateToken(refreshToken); // 변경 감지에 의해 업데이트
        }

        return LoginResponse.builder()
                .tokenType("Bearer")
                .accessToken(accessToken)
                .accessTokenExpiresIn(accessTokenTtlMs())
                .refreshToken(refreshToken)
                .refreshTokenExpiresIn(refreshTokenTtlMs())
                .userId(user.getId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImage().getImageUrl())
                .profileBackgroundHexCode(user.getProfileBackground().getColorHexCode())
                .firstLogin(firstLogin)
                .build();
    }

    @Transactional
    public ReissueResult reissue(TokenReissueRequest request) {
        final String requestRt = request.getRefreshToken();
        if (requestRt == null || requestRt.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "리프레시 토큰은 필수입니다.");
        }

        // 1) RT 유효성 검증(만료/서명 등)
        if (!jwtProvider.validateToken(requestRt)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "유효하지 않은 리프레시 토큰입니다.");
        }

        // 2) subject(userId) 파싱
        Long userId = jwtProvider.getUserId(requestRt);

        // 3) 저장된 RT 조회 및 일치 확인
        RefreshToken saved = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "리프레시 토큰이 존재하지 않습니다."));

        if (!saved.getToken().equals(requestRt)) {
            refreshTokenRepository.delete(saved);
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "리프레시 토큰이 일치하지 않습니다.");
        }

        // 4) 새 AT/RT 발급
        String newAccessToken = jwtProvider.createAccessToken(userId);
        String newRefreshToken = jwtProvider.createRefreshToken(userId);
        saved.updateToken(newRefreshToken);

        // 5) 서비스 결과 반환(AT는 바디, RT는 컨트롤러에서 HttpOnly 쿠키로 내려줌)
        return new ReissueResult(
                newAccessToken,
                jwtProps.getExpirationTime(),
                newRefreshToken,
                jwtProps.getRefreshExpirationTime()
        );
    }

}
