package com.matzip.place.domain;

import com.matzip.common.entity.BaseEntity;
import com.matzip.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "place_like")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceLike extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "place_like_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    private PlaceLike(User user, Place place) {
        this.user = user;
        this.place = place;
    }

    public static PlaceLike of(User user, Place place) {
        return new PlaceLike(user, place);
    }
}
