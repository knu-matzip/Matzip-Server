package com.matzip.lottery.controller.response;

import com.matzip.lottery.domain.LotteryEvent;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ParticipatedEventResponse(
        Long eventId,
        LotteryEventResponse.PrizeResponse prize,
        int totalWinnersCount,
        int participantsCount,
        LocalDateTime eventEndDate
) {

    public static ParticipatedEventResponse of(LotteryEvent event, int participantsCount) {
        return ParticipatedEventResponse.builder()
                .eventId(event.getId())
                .prize(LotteryEventResponse.PrizeResponse.from(event.getPrize()))
                .totalWinnersCount(event.getWinnersCount())
                .participantsCount(participantsCount)
                .eventEndDate(event.getEndDateTime())
                .build();
    }
}
