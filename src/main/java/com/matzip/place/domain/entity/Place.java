package com.matzip.place.domain.entity;

import com.matzip.common.entity.BaseEntity;
import com.matzip.place.domain.Campus;
import com.matzip.place.domain.PlaceStatus;
import com.matzip.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "place")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AttributeOverrides({
    @AttributeOverride(name = "id", column = @Column(name = "place_id")),
    @AttributeOverride(name = "createdAt", column = @Column(name = "created_at")),
    @AttributeOverride(name = "updatedAt", column = @Column(name = "updated_at"))
})
public class Place extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "campus", nullable = false)
    private Campus campus;

    @Column(name = "kakao_place_id", unique = true, nullable = false)
    private String kakaoPlaceId;

    @Column(name = "place_name", nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "like_count", nullable = false)
    private int likeCount = 0; // 기본값을 0으로 초기화

    @Column(name = "view_count")
    private int viewCount = 0;

    @ManyToOne
    @JoinColumn(name = "registered_by")
    private User registeredBy; // 등록자 (nullable - 비회원도 등록 가능)

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PlaceStatus status = PlaceStatus.PENDING; // 기본값은 승인 대기

    @OneToMany(mappedBy = "place")
    private Set<PlaceCategory> placeCategories = new HashSet<>();

    @OneToMany(mappedBy = "place")
    private Set<PlaceTag> placeTags = new HashSet<>();

    @OneToMany(mappedBy = "place")
    private List<Menu> menus = new ArrayList<>();

    @OneToMany(mappedBy = "place")
    private List<Photo> photos = new ArrayList<>();

    @Builder
    private Place(Long id, String kakaoPlaceId, Campus campus, String name, String address, double latitude, double longitude, String description, User registeredBy, PlaceStatus status) {
        super(id);
        this.kakaoPlaceId = kakaoPlaceId;
        this.campus = campus;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.registeredBy = registeredBy;
        this.status = status != null ? status : PlaceStatus.PENDING;
    }


    // Place를 승인 상태로 변경
    public void approve() {
        this.status = PlaceStatus.APPROVED;
    }

    public void reject() {
        this.status = PlaceStatus.REJECTED;
    }

    // 승인된 Place인지 확인
    public boolean isApproved() {
        return this.status == PlaceStatus.APPROVED;
    }

    // 승인 대기 중인 Place인지 확인
    public boolean isPending() {
        return this.status == PlaceStatus.PENDING;
    }

    public List<Category> getCategories() {
        return this.placeCategories.stream()
                .map(PlaceCategory::getCategory)
                .sorted(Comparator.comparing(Category::getId))
                .toList();
    }

    public List<Tag> getTags() {
        return this.placeTags.stream()
                .map(PlaceTag::getTag)
                .sorted(Comparator.comparing(Tag::getId))
                .toList();
    }

    public void incrementLikeCount() {
        this.likeCount++;
    }

    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }
}
