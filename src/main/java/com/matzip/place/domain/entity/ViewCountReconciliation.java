package com.matzip.place.domain.entity;

import com.matzip.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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
