package com.matzip.lottery.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class LotteryEntriesTest {

    @Test
    @DisplayName("userId 기반 엔트리를 사용자별로 집계한다")
    void countEntriesByUserId() {
        LotteryEvent lotteryEvent = LotteryEvent.builder().build();
        List<LotteryEntry> entries = List.of(
                LotteryEntry.of(lotteryEvent, 1L, 10L),
                LotteryEntry.of(lotteryEvent, 1L, 11L),
                LotteryEntry.of(lotteryEvent, 2L, 12L)
        );

        LotteryEntries lotteryEntries = new LotteryEntries(entries);

        assertThat(lotteryEntries.getParticipantsCount()).isEqualTo(2);
        assertThat(lotteryEntries.countEntriesByUser(1L)).isEqualTo(2);
        assertThat(lotteryEntries.countEntriesByUser(2L)).isEqualTo(1);
    }

    @Test
    @DisplayName("기존 ticket 기반 엔트리도 fallback으로 당첨자 추첨 대상에 포함된다")
    void drawWithLegacyTicketFallback() {
        LotteryEvent lotteryEvent = LotteryEvent.builder().build();
        Ticket legacyTicket = Ticket.builder()
                .userId(3L)
                .placeId(30L)
                .status(Ticket.Status.ACTIVE)
                .build();

        LotteryEntries lotteryEntries = new LotteryEntries(List.of(
                LotteryEntry.of(lotteryEvent, 1L, 10L),
                LotteryEntry.fromTicket(lotteryEvent, legacyTicket)
        ));

        Set<Long> winners = lotteryEntries.draw(2);

        assertThat(winners).contains(1L, 3L);
    }
}
