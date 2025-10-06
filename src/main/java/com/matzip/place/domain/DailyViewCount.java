package com.matzip.place.domain;

import com.matzip.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "daily_view_count")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyViewCount extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Column(name = "view_date", nullable = false)
    private LocalDate viewDate;

    @Column(nullable = false)
    private int count;

    public DailyViewCount(Place place, LocalDate viewDate, int count) {
        this.place = place;
        this.viewDate = viewDate;
        this.count = count;
    }

    public void incrementCount() {
        this.count++;
    }
}
