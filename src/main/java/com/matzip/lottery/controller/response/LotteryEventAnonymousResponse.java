package com.matzip.lottery.controller.response;

import com.matzip.lottery.domain.LotteryEvent;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record LotteryEventAnonymousResponse(Long eventId, LotteryEventResponse.PrizeResponse prize,
                                            int totalWinnersCount, int participantsCount, LocalDateTime eventEndDate)
        implements LotteryEventView {

    public static LotteryEventAnonymousResponse empty() {
        return LotteryEventAnonymousResponse.builder()
                .build();
    }

    public static LotteryEventAnonymousResponse of(LotteryEvent lotteryEvent, int participantsCount) {
        return LotteryEventAnonymousResponse.builder()
                .eventId(lotteryEvent.getId())
                .prize(LotteryEventResponse.PrizeResponse.from(lotteryEvent.getPrize()))
                .totalWinnersCount(lotteryEvent.getWinnersCount())
                .participantsCount(participantsCount)
                .eventEndDate(lotteryEvent.getEndDateTime())
                .build();
    }
}

