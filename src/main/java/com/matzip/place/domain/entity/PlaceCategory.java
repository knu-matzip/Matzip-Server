package com.matzip.place.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "place_category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(PlaceCategoryId.class) // 복합키를 사용할 클래스를 지정
public class PlaceCategory {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private Place place;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    public PlaceCategory(Place place, Category category, int displayOrder) {
        this.place = place;
        this.category = category;
        this.displayOrder = displayOrder;
    }

    public PlaceCategory(Place place, Category category) {
        this(place, category, 0);
    }
}
