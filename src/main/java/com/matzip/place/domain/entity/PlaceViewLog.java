package com.matzip.place.domain.entity;

import com.matzip.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 맛집 상세 조회 1건의 원본 로그. 조회수 일별 검증 보정의 기준 데이터다.
 * 조회 시각은 createdAt을 사용하고, 쓰기를 가볍게 하려고 place는 연관관계 없이 id만 둔다.
 */
@Entity
@Table(name = "place_view_log",
        indexes = @Index(name = "idx_place_view_log_date_place", columnList = "view_date, place_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceViewLog extends BaseEntity {

    @Column(name = "place_id", nullable = false)
    private Long placeId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "view_date", nullable = false)
    private LocalDate viewDate;

    private PlaceViewLog(Long placeId, Long userId, LocalDate viewDate) {
        this.placeId = placeId;
        this.userId = userId;
        this.viewDate = viewDate;
    }

    public static PlaceViewLog of(Long placeId, Long userId, LocalDate viewDate) {
        return new PlaceViewLog(placeId, userId, viewDate);
    }
}
