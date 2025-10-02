package com.matzip.auth.application;

import com.matzip.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ProfileAssignmentTest {

    @InjectMocks
    private ProfileAssignment profileAssignment;

    @Test
    @DisplayName("랜덤 프로필 할당 - 이미지와 배경색이 제대로 설정되는지 테스트")
    void assignRandomProfile_ShouldSetProfileImageAndBackground() {
        // given
        User user = User.builder()
                .kakaoId(12345L)
                .nickname("테스트닉네임")
                .build();

        // when
        profileAssignment.assignRandomProfile(user);

        // then
        assertThat(user.getProfileImage()).isNotNull();
        assertThat(user.getProfileBackground()).isNotNull();
        assertThat(user.getProfileImage().getImageUrl()).isNotBlank();
        assertThat(user.getProfileBackground().getColorHexCode()).isNotBlank();
        
        assertThat(user.getProfileImage().getImageUrl()).startsWith("/images/profiles/");
        assertThat(user.getProfileImage().getImageUrl()).endsWith(".png");
        
        // 배경색이 6자리 헥스 코드인지 확인
        assertThat(user.getProfileBackground().getColorHexCode()).matches("[0-9A-F]{6}");
    }

}
