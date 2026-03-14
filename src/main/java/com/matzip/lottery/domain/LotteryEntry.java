package com.matzip.lottery.domain;

import com.matzip.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Builder;
import lombok.Getter;

@Getter
@Entity
@Table(
        name = "lottery_event_ticket",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_lottery_event_ticket_event_place",
                columnNames = {"lottery_event_id", "place_id"}
        )
)
public class LotteryEntry extends BaseEntity {

    @JoinColumn(name = "lottery_event_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private LotteryEvent lotteryEvent;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "place_id")
    private Long placeId;

    protected LotteryEntry() {
    }

    @Builder
    public LotteryEntry(LotteryEvent lotteryEvent, Long userId, Long placeId) {
        this.lotteryEvent = lotteryEvent;
        this.userId = userId;
        this.placeId = placeId;
    }

    public static LotteryEntry of(LotteryEvent lotteryEvent, Long userId, Long placeId) {
        return LotteryEntry.builder()
                .lotteryEvent(lotteryEvent)
                .userId(userId)
                .placeId(placeId)
                .build();
    }
}
