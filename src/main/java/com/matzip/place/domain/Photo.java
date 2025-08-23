package com.matzip.place.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "photo")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "photo_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false) // 연관관계의 주인
    private Place place;

    @Column(name = "photo_url", length = 2048)
    private String photoUrl;

    @Column(name = "display_order")
    private int displayOrder;

    @CreationTimestamp // 엔티티가 처음 저장될 때 현재 시간이 자동으로 저장됨
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 카카오맵 API를 통해 사진 정보를 가져온 시각
    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;

    @Builder
    private Photo(Place place, String photoUrl, Integer displayOrder, LocalDateTime fetchedAt) {
        this.place = place;
        this.photoUrl = photoUrl;
        this.displayOrder = displayOrder;
        this.fetchedAt = fetchedAt;
    }

}
