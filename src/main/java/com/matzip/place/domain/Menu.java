package com.matzip.place.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "menu")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false) // 연관관계의 주인
    private Place place;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Integer price;

    @Column(name = "is_recommended", nullable = false)
    private Boolean isRecommended = false;

    @Builder
    public Menu(Place place, String name, Integer price, Boolean isRecommended) {
        this.place = place;
        this.name = name;
        this.price = price;
        this.isRecommended = isRecommended;
    }

}