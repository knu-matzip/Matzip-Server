package com.matzip.lottery.domain;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class LotteryEntries {

    private final Collection<LotteryEntry> lotteryEntries;
    private final Map<Long, Integer> ticketsCountByUser;

    public LotteryEntries(Collection<LotteryEntry> lotteryEntries) {
        this.lotteryEntries = lotteryEntries;
        this.ticketsCountByUser = calculateTicketsCount();
    }

    private Map<Long, Integer> calculateTicketsCount() {
        Map<Long, Integer> counts = new HashMap<>();
        for (LotteryEntry lotteryEntry : lotteryEntries) {
            Long userId = lotteryEntry.getTicket().getUserId();
            counts.merge(userId, 1, Integer::sum);
        }
        return counts;
    }

    public int getParticipantsCount() {
        return ticketsCountByUser.size();
    }

    public int countTicketsByUser(Long userId) {
        return ticketsCountByUser.getOrDefault(userId, 0);
    }
}
