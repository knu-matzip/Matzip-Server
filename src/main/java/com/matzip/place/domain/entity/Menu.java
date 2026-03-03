package com.matzip.place.domain.entity;

import com.matzip.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "menu")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Menu extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false) // 연관관계의 주인
    private Place place;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private boolean isRecommended = false;

    @Builder
    public Menu(Place place, String name, Integer price, Boolean isRecommended) {
        this.place = place;
        this.name = name;
        this.price = price;
        this.isRecommended = isRecommended;
    }

}