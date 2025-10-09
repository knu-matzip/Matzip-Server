package com.matzip.user.api.response;

import com.matzip.user.domain.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserProfileResponseDto {

    private String nickname;
    private String profileImageUrl;
    private String profileBackgroundHexCode;

    public static UserProfileResponseDto from(User user, String imageBaseUrl) {
        return UserProfileResponseDto.builder()
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImage() != null ? user.getProfileImage().getFullUrl(imageBaseUrl) : null)
                .profileBackgroundHexCode(user.getProfileBackground() != null ? user.getProfileBackground().getColorHexCode() : null)
                .build();
    }
}
