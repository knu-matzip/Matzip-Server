package com.matzip.user.dto.response;

import com.matzip.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserProfileResponseDto {

    @Schema(description = "닉네임", example = "행복한 다람쥐")
    private String nickname;

    @Schema(description = "프로필 이미지 URL", example = "https://knu-matzip.co.kr/images/profiles/tiger.png")
    private String profileImageUrl;

    @Schema(description = "프로필 배경 색상 HEX 코드", example = "BEE1E6")
    private String profileBackgroundHexCode;

    public static UserProfileResponseDto from(User user, String imageBaseUrl) {
        return UserProfileResponseDto.builder()
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImage() != null ? user.getProfileImage().getFullUrl(imageBaseUrl) : null)
                .profileBackgroundHexCode(user.getProfileBackground() != null ? user.getProfileBackground().getColorHexCode() : null)
                .build();
    }
}
