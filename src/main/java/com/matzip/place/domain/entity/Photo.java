package com.matzip.place.domain.entity;

import com.matzip.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "photo")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Photo extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false) // 연관관계의 주인
    private Place place;

    @Column(length = 2048)
    private String photoUrl;

    @Column
    private int displayOrder;

    // 카카오맵 API를 통해 사진 정보를 가져온 시각
    @Column(nullable = false)
    private LocalDateTime fetchedAt;

    @Builder
    private Photo(Place place, String photoUrl, Integer displayOrder, LocalDateTime fetchedAt) {
        this.place = place;
        this.photoUrl = photoUrl;
        this.displayOrder = displayOrder;
        this.fetchedAt = fetchedAt;
    }

}
