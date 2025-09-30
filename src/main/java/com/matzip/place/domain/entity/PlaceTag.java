package com.matzip.place.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "place_tag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(PlaceTagId.class) // 복합키를 사용할 클래스를 지정
public class PlaceTag {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private Place place;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    private Tag tag;

    public PlaceTag(Place place, Tag tag) {
        this.place = place;
        this.tag = tag;
    }
}
