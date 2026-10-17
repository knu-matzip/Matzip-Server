package com.matzip.place.domain.entity;

import com.matzip.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 하루치 조회수 검증 보정 결과. 보정 전 집계와 원본 로그를 비교한 값을 남긴다.
 * - missingCount: 로그보다 집계가 적었던 만큼의 합 (비동기 증가 유실분)
 * - overCount: 로그보다 집계가 많았던 만큼의 합 (집계 후 롤백된 요청 등)
 */
@Entity
@Table(name = "view_count_reconciliation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ViewCountReconciliation extends BaseEntity {

    @Column(name = "target_date", nullable = false, unique = true)
    private LocalDate targetDate;

    @Column(name = "logged_count", nullable = false)
    private long loggedCount;

    @Column(name = "aggregated_count", nullable = false)
    private long aggregatedCount;

    @Column(name = "missing_count", nullable = false)
    private long missingCount;

    @Column(name = "over_count", nullable = false)
    private long overCount;

    public ViewCountReconciliation(LocalDate targetDate, long loggedCount, long aggregatedCount,
                                   long missingCount, long overCount) {
        this.targetDate = targetDate;
        this.loggedCount = loggedCount;
        this.aggregatedCount = aggregatedCount;
        this.missingCount = missingCount;
        this.overCount = overCount;
    }

    public double lossRate() {
        if (loggedCount == 0) {
            return 0.0;
        }
        return (double) missingCount / loggedCount;
    }
}
