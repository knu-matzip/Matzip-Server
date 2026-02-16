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
    private boolean isDrawn;

    @Column(nullable = false)
    private LocalDateTime endDateTime;

    protected LotteryEvent() {
    }

    @Builder
    public LotteryEvent(Prize prize, int winnersCount, boolean isDrawn, LocalDateTime endDateTime) {
        this.prize = prize;
        this.winnersCount = winnersCount;
        this.isDrawn = isDrawn;
        this.endDateTime = endDateTime;
    }

    public boolean isReadyForDraw(LocalDateTime currentDateTime) {
        return currentDateTime.isAfter(endDateTime) && !isDrawn;
    }

    public void completeDraw() {
        if (isDrawn) {
            throw new IllegalStateException("이미 추첨이 완료되었습니다.");
        }

        this.isDrawn = true;
    }
}
