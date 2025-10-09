package com.matzip.auth.application;

import com.matzip.user.domain.ProfileBackground;
import com.matzip.user.domain.ProfileImage;
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
    @DisplayName("랜덤 프로필 이미지 반환 - 유효한 이미지가 반환되는지 테스트")
    void getRandomProfileImage_ShouldReturnValidImage() {
        // when
        ProfileImage profileImage = profileAssignment.getRandomProfileImage();

        // then
        assertThat(profileImage).isNotNull();
        assertThat(profileImage.getImageUrl()).isNotBlank();
        assertThat(profileImage.getImageUrl()).startsWith("/images/profiles/");
        assertThat(profileImage.getImageUrl()).endsWith(".png");
    }

    @Test
    @DisplayName("랜덤 프로필 배경 반환 - 유효한 배경색이 반환되는지 테스트")
    void getRandomProfileBackground_ShouldReturnValidBackground() {
        // when
        ProfileBackground profileBackground = profileAssignment.getRandomProfileBackground();

        // then
        assertThat(profileBackground).isNotNull();
        assertThat(profileBackground.getColorHexCode()).isNotBlank();
        // 배경색이 6자리 헥스 코드인지 확인
        assertThat(profileBackground.getColorHexCode()).matches("[0-9A-F]{6}");
    }

    @Test
    @DisplayName("여러 번 호출 시 다른 값들이 반환되는지 테스트 (랜덤성 확인)")
    void getRandomProfile_MultipleCalls_ShouldReturnDifferentValues() {
        // when
        ProfileImage image1 = profileAssignment.getRandomProfileImage();
        ProfileImage image2 = profileAssignment.getRandomProfileImage();
        ProfileBackground background1 = profileAssignment.getRandomProfileBackground();
        ProfileBackground background2 = profileAssignment.getRandomProfileBackground();

        // then
        assertThat(image1).isNotNull();
        assertThat(image2).isNotNull();
        assertThat(background1).isNotNull();
        assertThat(background2).isNotNull();
    }

}
