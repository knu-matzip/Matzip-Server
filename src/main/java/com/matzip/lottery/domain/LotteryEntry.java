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

    @JoinColumn(name = "ticket_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Ticket ticket;

    protected LotteryEntry() {
    }

    @Builder
    public LotteryEntry(LotteryEvent lotteryEvent, Long userId, Long placeId, Ticket ticket) {
        this.lotteryEvent = lotteryEvent;
        this.userId = userId;
        this.placeId = placeId;
        this.ticket = ticket;
    }

    public static LotteryEntry of(LotteryEvent lotteryEvent, Long userId, Long placeId) {
        return LotteryEntry.builder()
                .lotteryEvent(lotteryEvent)
                .userId(userId)
                .placeId(placeId)
                .build();
    }

    public static LotteryEntry fromTicket(LotteryEvent lotteryEvent, Ticket ticket) {
        return LotteryEntry.builder()
                .lotteryEvent(lotteryEvent)
                .userId(ticket.getUserId())
                .placeId(ticket.getPlaceId())
                .ticket(ticket)
                .build();
    }

    public Long getUserId() {
        if (userId != null) {
            return userId;
        }
        return ticket != null ? ticket.getUserId() : null;
    }

    public Long getPlaceId() {
        if (placeId != null) {
            return placeId;
        }
        return ticket != null ? ticket.getPlaceId() : null;
    }
}
