package com.matzip.lottery.controller.response;

import com.matzip.lottery.domain.LotteryEvent;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ParticipatedEventResponseDto(
        Long eventId,
        LotteryEventResponseDto.PrizeResponse prize,
        int totalWinnersCount,
        int participantsCount,
        LocalDateTime eventEndDate
) {

    public static ParticipatedEventResponseDto of(LotteryEvent event, int participantsCount) {
        return ParticipatedEventResponseDto.builder()
                .eventId(event.getId())
                .prize(LotteryEventResponseDto.PrizeResponse.from(event.getPrize()))
                .totalWinnersCount(event.getWinnersCount())
                .participantsCount(participantsCount)
                .eventEndDate(event.getEndDateTime())
                .build();
    }
}
