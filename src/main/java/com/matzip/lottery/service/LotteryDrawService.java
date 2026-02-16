package com.matzip.lottery.service;

import com.matzip.lottery.domain.LotteryEntries;
import com.matzip.lottery.domain.LotteryEntry;
import com.matzip.lottery.domain.LotteryEvent;
import com.matzip.lottery.domain.Winner;
import com.matzip.lottery.repository.LotteryEntryRepository;
import com.matzip.lottery.repository.LotteryEventRepository;
import com.matzip.lottery.repository.WinnerRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class LotteryDrawService {

    private final LotteryEventRepository lotteryEventRepository;
    private final LotteryEntryRepository lotteryEntryRepository;
    private final WinnerRepository winnerRepository;

    public LotteryDrawService(LotteryEventRepository lotteryEventRepository, LotteryEntryRepository lotteryEntryRepository, WinnerRepository winnerRepository) {
        this.lotteryEventRepository = lotteryEventRepository;
        this.lotteryEntryRepository = lotteryEntryRepository;
        this.winnerRepository = winnerRepository;
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void drawWinners() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        lotteryEventRepository.findRecentlyEndedEvent(currentDateTime)
                .filter(lotteryEvent -> lotteryEvent.isReadyForDraw(currentDateTime))
                .ifPresent(this::processLottery);
    }

    private void processLottery(LotteryEvent lotteryEvent) {
        List<LotteryEntry> entries = lotteryEntryRepository.findByLotteryEvent(lotteryEvent);
        LotteryEntries lotteryEntries = new LotteryEntries(entries);

        int winnersCount = lotteryEvent.getWinnersCount();
        Set<Long> winnerUserIds = lotteryEntries.draw(winnersCount);

        List<Winner> winners = winnerUserIds.stream()
                .map(userId -> Winner.builder()
                        .userId(userId)
                        .eventId(lotteryEvent.getId())
                        .build())
                .toList();
        winnerRepository.saveAll(winners);

        lotteryEvent.completeDraw();
        lotteryEventRepository.save(lotteryEvent);
    }
}
