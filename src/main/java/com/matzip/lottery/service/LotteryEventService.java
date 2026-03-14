package com.matzip.lottery.service;

import com.matzip.common.exception.BusinessException;
import com.matzip.common.exception.code.ErrorCode;
import com.matzip.lottery.controller.request.ApplyEventRequest;
import com.matzip.lottery.controller.response.ApplyEventResponse;
import com.matzip.lottery.controller.response.EventEntryResultResponse;
import com.matzip.lottery.controller.response.LotteryEventAnonymousResponse;
import com.matzip.lottery.controller.response.LotteryEventResponse;
import com.matzip.lottery.controller.response.LotteryEventView;
import com.matzip.lottery.controller.response.ParticipatedEventResponse;
import com.matzip.lottery.domain.LotteryEntries;
import com.matzip.lottery.domain.LotteryEntry;
import com.matzip.lottery.domain.LotteryEvent;
import com.matzip.lottery.domain.WinnerContact;
import com.matzip.lottery.infra.discord.DiscordWinnerNotificationService;
import com.matzip.lottery.repository.LotteryEntryRepository;
import com.matzip.lottery.repository.LotteryEventRepository;
import com.matzip.lottery.repository.WinnerContactRepository;
import com.matzip.lottery.repository.WinnerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class LotteryEventService {

    private final LotteryEventRepository lotteryEventRepository;
    private final LotteryEntryRepository lotteryEntryRepository;
    private final WinnerRepository winnerRepository;
    private final WinnerContactRepository winnerContactRepository;
    private final DiscordWinnerNotificationService discordWinnerNotificationService;

    public LotteryEventService(LotteryEventRepository lotteryEventRepository, LotteryEntryRepository lotteryEntryRepository,
                               WinnerRepository winnerRepository,
                               WinnerContactRepository winnerContactRepository,
                               DiscordWinnerNotificationService discordWinnerNotificationService) {
        this.lotteryEventRepository = lotteryEventRepository;
        this.lotteryEntryRepository = lotteryEntryRepository;
        this.winnerRepository = winnerRepository;
        this.winnerContactRepository = winnerContactRepository;
        this.discordWinnerNotificationService = discordWinnerNotificationService;
    }

    @Transactional(readOnly = true)
    public LotteryEventView getCurrentEvent(Long userId) {
        return lotteryEventRepository.findCurrentEvent(LocalDateTime.now())
                .map(currentEvent -> {
                    List<LotteryEntry> entries = lotteryEntryRepository.findByLotteryEvent(currentEvent);
                    LotteryEntries lotteryEntries = new LotteryEntries(entries);
                    int participantsCount = lotteryEntries.getParticipantsCount();

                    if (userId == null) {
                        return LotteryEventAnonymousResponse.of(currentEvent, participantsCount);
                    }

                    int usedTicketsCount = lotteryEntries.countEntriesByUser(userId);

                    return LotteryEventResponse.of(currentEvent, participantsCount, usedTicketsCount);
                })
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<ParticipatedEventResponse> getParticipatedEvents(Long userId) {
        List<LotteryEvent> events = lotteryEntryRepository.findDistinctEndedLotteryEventsByUserId(userId, LocalDateTime.now());

        return events.stream()
                .map(event -> {
                    int participantsCount = lotteryEntryRepository.countParticipantsByLotteryEvent(event);
                    return ParticipatedEventResponse.of(event, participantsCount);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public EventEntryResultResponse getEntryResult(Long eventId, Long userId) {
        LotteryEvent event = lotteryEventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이벤트가 존재하지 않습니다. eventId: " + eventId));

        int usedTicketsCount = lotteryEntryRepository.countByLotteryEventIdAndUserId(eventId, userId);
        if (usedTicketsCount == 0) {
            throw new BusinessException(ErrorCode.EVENT_NOT_PARTICIPATED);
        }

        LocalDateTime now = LocalDateTime.now();
        if (!event.getEndDateTime().isBefore(now) || !event.isDrawn()) {
            throw new BusinessException(ErrorCode.DRAW_NOT_COMPLETED, "아직 이벤트 추첨이 진행되지 않았습니다.");
        }

        int participantsCount = lotteryEntryRepository.countParticipantsByLotteryEvent(event);
        boolean isWinner = winnerRepository.findByUserIdAndEventId(userId, eventId).isPresent();
        boolean isPhoneSubmitted = winnerContactRepository.findByUserIdAndEventId(userId, eventId).isPresent();

        return EventEntryResultResponse.of(event, participantsCount, usedTicketsCount, isWinner, isPhoneSubmitted);
    }

    @Transactional
    public ApplyEventResponse applyForPrize(Long eventId, Long userId, ApplyEventRequest request) {
        LotteryEvent event = lotteryEventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이벤트가 존재하지 않습니다. eventId: " + eventId));

        LocalDateTime now = LocalDateTime.now();
        if (!event.getEndDateTime().isBefore(now) || !event.isDrawn()) {
            throw new BusinessException(ErrorCode.DRAW_NOT_COMPLETED);
        }
        if (winnerRepository.findByUserIdAndEventId(userId, eventId).isEmpty()) {
            throw new BusinessException(ErrorCode.EVENT_NOT_WINNER);
        }

        if (!request.isAllAgreed()) {
            throw new BusinessException(ErrorCode.AGREEMENT_REQUIRED);
        }

        WinnerContact contact = winnerContactRepository.findByUserIdAndEventId(userId, eventId)
                .map(existing -> {
                    existing.update(request.phoneNumber(), request.agreements().termsAgreed(), request.agreements().privacyAgreed());
                    return winnerContactRepository.save(existing);
                })
                .orElseGet(() -> winnerContactRepository.save(WinnerContact.builder()
                        .userId(userId)
                        .eventId(eventId)
                        .phoneNumber(request.phoneNumber())
                        .termsAgreed(request.agreements().termsAgreed())
                        .privacyAgreed(request.agreements().privacyAgreed())
                        .build()));

        discordWinnerNotificationService.notifyWinnerContactSubmitted(event, userId, request.phoneNumber());

        return ApplyEventResponse.from(contact);
    }

    @Transactional
    public boolean enterCurrentEventOnPlaceApproval(Long userId, Long placeId) {
        return lotteryEventRepository.findCurrentEvent(LocalDateTime.now())
                .map(currentEvent -> enterOnPlaceApproval(currentEvent, userId, placeId))
                .orElse(false);
    }

    private boolean enterOnPlaceApproval(LotteryEvent lotteryEvent, Long userId, Long placeId) {
        if (lotteryEntryRepository.existsByLotteryEvent_IdAndPlaceId(lotteryEvent.getId(), placeId)) {
            log.info("[자동 응모 스킵] 이미 응모 처리된 placeId: {}, eventId: {}", placeId, lotteryEvent.getId());
            return false;
        }

        try {
            lotteryEntryRepository.save(LotteryEntry.of(lotteryEvent, userId, placeId));
            return true;
        } catch (DataIntegrityViolationException exception) {
            log.warn("[자동 응모 중복 감지] placeId: {}, eventId: {}", placeId, lotteryEvent.getId());
            return false;
        }
    }
}
