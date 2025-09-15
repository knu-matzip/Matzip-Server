package com.matzip.place.domain;

import com.matzip.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "place")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "place_id")
    private Long id;

    // user_id (FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User creator;

    @Column(name = "kakao_place_id", unique = true, nullable = false)
    private Long kakaoPlaceId;

    @Column(name = "place_name", nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 8)
    private BigDecimal longitude;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "like_count", nullable = false)
    private int likeCount = 0; // 기본값을 0으로 초기화


    @Builder
    private Place(Long id, User creator, Long kakaoPlaceId, String name, String address, BigDecimal latitude, BigDecimal longitude, String description) {
        this.id = id;
        this.creator = creator;
        this.kakaoPlaceId = kakaoPlaceId;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
    }
}