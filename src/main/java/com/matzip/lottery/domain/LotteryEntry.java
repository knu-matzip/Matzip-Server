package com.matzip.lottery.domain;

import com.matzip.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;

@Getter
@Entity
@Table(name = "lottery_event_ticket")
public class LotteryEntry extends BaseEntity {

    @JoinColumn(name = "lottery_event_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private LotteryEvent lotteryEvent;

    @JoinColumn(name = "ticket_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Ticket ticket;

    protected LotteryEntry() {
    }

    @Builder
    public LotteryEntry(LotteryEvent lotteryEvent, Ticket ticket) {
        this.lotteryEvent = lotteryEvent;
        this.ticket = ticket;
    }
}
