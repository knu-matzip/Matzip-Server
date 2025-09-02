package com.matzip.place.domain;

import jakarta.persistence.*;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "campus", nullable = false)
    private Campus campus;

    @Column(name = "kakao_place_id", unique = true, nullable = false)
    private String kakaoPlaceId;

    @Column(name = "place_name", nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0; // 기본값을 0으로 초기화


    @Builder
    private Place(String kakaoPlaceId, Campus campus, String name, String address, BigDecimal latitude, BigDecimal longitude, String description) {
        this.kakaoPlaceId = kakaoPlaceId;
        this.campus = campus;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
    }
}