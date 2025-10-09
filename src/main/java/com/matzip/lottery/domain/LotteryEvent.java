package com.matzip.lottery.domain;

import com.matzip.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
public class LotteryEvent extends BaseEntity {

    @Embedded
    private Prize prize;

    @Column(nullable = false)
    private int winnersCount;

    @Column(nullable = false)
    private LocalDateTime endDateTime;

    protected LotteryEvent() {
    }

    @Builder
    public LotteryEvent(Prize prize, int winnersCount, LocalDateTime endDateTime) {
        this.prize = prize;
        this.winnersCount = winnersCount;
        this.endDateTime = endDateTime;
    }
}
