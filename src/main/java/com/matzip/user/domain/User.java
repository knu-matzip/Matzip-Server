package com.matzip.user.domain;

import com.matzip.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;

@Entity
@Table(name = "users")
@Getter
public class User extends BaseEntity {

    @Column(name = "kakao_id", nullable = false, unique = true)
    private Long kakaoId; // 카카오에서 제공하는 고유 식별자

    @Column(nullable = false, length = 30)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_image", length = 40)
    private ProfileImage profileImage;

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_background", length = 20)
    private ProfileBackground profileBackground;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UserStatus status = UserStatus.ACTIVE;

    protected User() {
    }

    @Builder
    private User(Long id, Long kakaoId, String nickname) {
        super(id);
        this.kakaoId = kakaoId;
        this.nickname = nickname;
    }

    public void setProfile(ProfileImage image, ProfileBackground background) {
        this.profileImage = image;
        this.profileBackground = background;
    }
}
