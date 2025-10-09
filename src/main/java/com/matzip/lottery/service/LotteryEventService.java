package com.matzip.lottery.service;

import com.matzip.common.exception.BusinessException;
import com.matzip.common.exception.code.ErrorCode;
import com.matzip.lottery.controller.response.LotteryEventResponse;
import com.matzip.lottery.domain.LotteryEntries;
import com.matzip.lottery.domain.LotteryEntry;
import com.matzip.lottery.domain.LotteryEvent;
import com.matzip.lottery.domain.Ticket;
import com.matzip.lottery.repository.LotteryEntryRepository;
import com.matzip.lottery.repository.LotteryEventRepository;
import com.matzip.lottery.repository.TicketRepository;
import org.springframework.data.domain.Sort;
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

    @Transactional
    public void enterLottery(Long eventId, int ticketsCount, Long userId) {
        LotteryEvent lotteryEvent = lotteryEventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이벤트가 존재하지 않습니다. eventId: " + eventId));
        validateLotteryEvent(lotteryEvent);

        Sort sort = Sort.by("createdAt");
        List<Ticket> remainingTickets = ticketRepository.findByUserIdAndStatus(userId, Ticket.Status.ACTIVE, sort);
        validateTickets(remainingTickets, ticketsCount);

        remainingTickets.stream()
                .limit(ticketsCount)
                .forEach(ticket -> doEnter(lotteryEvent, ticket));
    }

    private void doEnter(LotteryEvent lotteryEvent, Ticket ticket) {
        LotteryEntry lotteryEntry = new LotteryEntry(lotteryEvent, ticket);
        lotteryEntryRepository.save(lotteryEntry);
        ticket.use();
    }

    private void validateLotteryEvent(LotteryEvent lotteryEvent) {
        if (lotteryEvent.getEndDateTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.EVENT_ENDED);
        }
    }

    private void validateTickets(List<Ticket> remainingTickets, int ticketsCount) {
        int remainingTicketsCount = remainingTickets.size();
        if (remainingTicketsCount < ticketsCount) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_ENTRY_TICKETS,
                    String.format("응모권이 부족합니다. 현재 남은 응모권: %d", remainingTicketsCount));
        }
    }
}
