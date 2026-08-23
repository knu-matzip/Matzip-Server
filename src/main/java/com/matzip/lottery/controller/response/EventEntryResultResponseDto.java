package com.matzip.lottery.controller.response;

import com.matzip.lottery.domain.LotteryEvent;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record EventEntryResultResponseDto(
        Long eventId,
        LotteryEventResponseDto.PrizeResponse prize,
        int totalWinnersCount,
        int participantsCount,
        int usedTicketsCount,
        boolean isWinner,
        LocalDateTime eventEndDate,
        boolean isPhoneSubmitted
) {

    public static EventEntryResultResponseDto of(LotteryEvent event, int participantsCount, int usedTicketsCount,
                                              boolean isWinner, boolean isPhoneSubmitted) {
        return EventEntryResultResponseDto.builder()
                .eventId(event.getId())
                .prize(LotteryEventResponseDto.PrizeResponse.from(event.getPrize()))
                .totalWinnersCount(event.getWinnersCount())
                .participantsCount(participantsCount)
                .usedTicketsCount(usedTicketsCount)
                .isWinner(isWinner)
                .eventEndDate(event.getEndDateTime())
                .isPhoneSubmitted(isPhoneSubmitted)
                .build();
    }
}
