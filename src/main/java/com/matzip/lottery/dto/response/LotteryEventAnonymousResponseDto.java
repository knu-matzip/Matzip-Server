package com.matzip.lottery.dto.response;

import com.matzip.lottery.domain.LotteryEvent;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record LotteryEventAnonymousResponseDto(Long eventId, LotteryEventResponseDto.PrizeResponse prize,
                                            int totalWinnersCount, int participantsCount, LocalDateTime eventEndDate)
        implements LotteryEventView {

    public static LotteryEventAnonymousResponseDto empty() {
        return LotteryEventAnonymousResponseDto.builder()
                .build();
    }

    public static LotteryEventAnonymousResponseDto of(LotteryEvent lotteryEvent, int participantsCount) {
        return LotteryEventAnonymousResponseDto.builder()
                .eventId(lotteryEvent.getId())
                .prize(LotteryEventResponseDto.PrizeResponse.from(lotteryEvent.getPrize()))
                .totalWinnersCount(lotteryEvent.getWinnersCount())
                .participantsCount(participantsCount)
                .eventEndDate(lotteryEvent.getEndDateTime())
                .build();
    }
}

