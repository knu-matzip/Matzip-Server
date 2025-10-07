package com.matzip.lottery.service;

import com.matzip.lottery.controller.response.LotteryEventResponse;
import com.matzip.lottery.domain.LotteryEntries;
import com.matzip.lottery.domain.LotteryEntry;
import com.matzip.lottery.domain.Ticket;
import com.matzip.lottery.repository.LotteryEntryRepository;
import com.matzip.lottery.repository.LotteryEventRepository;
import com.matzip.lottery.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LotteryEventService {

    private final LotteryEventRepository lotteryEventRepository;
    private final TicketRepository ticketRepository;
    private final LotteryEntryRepository lotteryEntryRepository;

    public LotteryEventService(LotteryEventRepository lotteryEventRepository, TicketRepository ticketRepository, LotteryEntryRepository lotteryEntryRepository) {
        this.lotteryEventRepository = lotteryEventRepository;
        this.ticketRepository = ticketRepository;
        this.lotteryEntryRepository = lotteryEntryRepository;
    }

    @Transactional(readOnly = true)
    public LotteryEventResponse getCurrentEvent(Long userId) {
        return lotteryEventRepository.findCurrentEvent(LocalDateTime.now())
                .map(currentEvent -> {
                    List<LotteryEntry> entries = lotteryEntryRepository.findByLotteryEvent(currentEvent);
                    LotteryEntries lotteryEntries = new LotteryEntries(entries);

                    int participantsCount = lotteryEntries.getParticipantsCount();
                    int usedTicketsCount = lotteryEntries.countTicketsByUser(userId);
                    int remainingTicketsCount = ticketRepository.countByUserIdAndStatus(userId, Ticket.Status.ACTIVE);

                    return LotteryEventResponse.of(currentEvent, participantsCount, usedTicketsCount, remainingTicketsCount);
                })
                .orElseGet(LotteryEventResponse::empty);
    }
}
