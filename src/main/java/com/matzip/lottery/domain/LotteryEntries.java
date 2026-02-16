package com.matzip.lottery.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

    public Set<Long> draw(int winnersCount) {
        if (this.lotteryEntries.isEmpty() || winnersCount <= 0) {
            return Collections.emptySet();
        }

        Set<Long> winners = new HashSet<>();
        ArrayList<LotteryEntry> entries = new ArrayList<>(this.lotteryEntries);

        Collections.shuffle(entries);
        for (LotteryEntry entry : entries) {
            Long userId = entry.getTicket().getUserId();
            if (winners.add(userId) && winners.size() >= winnersCount) {
                break;
            }
        }

        return winners;
    }
}
