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
    @DisplayName("엔트리 기반으로 사용자 중복 없이 당첨자를 추첨한다")
    void drawWithEntryBasedUsers() {
        LotteryEvent lotteryEvent = LotteryEvent.builder().build();
        LotteryEntries lotteryEntries = new LotteryEntries(List.of(
                LotteryEntry.of(lotteryEvent, 1L, 10L),
                LotteryEntry.of(lotteryEvent, 3L, 30L)
        ));

        Set<Long> winners = lotteryEntries.draw(2);

        assertThat(winners).contains(1L, 3L);
    }
}
