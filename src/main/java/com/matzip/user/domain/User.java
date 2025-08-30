package com.matzip.user.domain;

import com.matzip.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

@Entity
@Table(name = "user")
@Getter
public class User extends BaseEntity {

    @Column(name = "kakao_id", nullable = false, unique = true)
    private String kakaoId; // 카카오에서 제공하는 고유 식별자

    @Column(nullable = false, length = 30)
    private String nickname;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UserStatus status = UserStatus.ACTIVE;

    public User() {
    }

    @Builder
    private User(String kakaoId, String nickname, String profileImageUrl) {
        this.kakaoId = kakaoId;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }
}