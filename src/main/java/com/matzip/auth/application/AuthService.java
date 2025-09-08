package com.matzip.auth.application;

import com.matzip.auth.api.dto.KakaoLoginRequest;
import com.matzip.auth.api.dto.LoginResponse;
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
    private RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProps; // 주입

    private final NickNameGenerator nickNameGenerator;
    private final ProfileImagePicker profileImagePicker;

    private long accessTokenTtlMs() {
        return jwtProps.getExpirationTime();
    }
    private long refreshTokenTtlMs() {
        return jwtProps.getRefreshExpirationTime();
    }

    @Transactional
    public LoginResponse login(KakaoLoginRequest loginRequest) {
        KakaoTokenResponse kakoToken = kakaoLoginApiClient.exchangeToken(loginRequest.getCode());
        if (kakoToken == null || kakoToken.getAccessToken() == null) {
            throw new BusinessException(ErrorCode.KAKAO_LOGIN_FAILED, "카카오 토큰 응답이 비어 있습니다.");
        }

        KakaoUserResponse kakaoUser = kakaoLoginApiClient.getUser(kakoToken.getAccessToken());
        if (kakaoUser == null || kakaoUser.getId() == null) {
            throw new BusinessException(ErrorCode.KAKAO_LOGIN_FAILED, "카카오 사용자 정보 조회 실패");
        }

        boolean firstLogin = false;
        User user = userRepository.findByKakaoId(kakaoUser.getId()).orElse(null);

        if (user == null) {
            String nickname = nickNameGenerator.generate();
            String profileUrl = profileImagePicker.pick();

            user = User.builder()
                    .kakaoId(kakaoUser.getId())
                    .nickname(nickname)
                    .profileImageUrl(profileUrl) // TODO: FK 전환 시 제거하고 ProfileImage 엔티티 참조로 변경
                    .build();

            user = userRepository.save(user);
            firstLogin = true;
        }

        String accessToken = jwtProvider.createAccessToken(user.getId());
        String refreshToken = jwtProvider.createRefreshToken(user.getId());

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
                .profileImageUrl(user.getProfileImageUrl()) // TODO: FK 전환 시 DTO 매핑에서 user.getProfileImage().getImageUrl() 사용
                .firstLogin(firstLogin)
                .build();
    }

}
