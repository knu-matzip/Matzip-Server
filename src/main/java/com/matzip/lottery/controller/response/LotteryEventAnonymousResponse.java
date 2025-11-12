package com.matzip.lottery.controller.response;

import com.matzip.lottery.domain.LotteryEvent;
import lombok.Builder;

@Builder
public record LotteryEventAnonymousResponse(LotteryEventResponse.PrizeResponse prize) implements LotteryEventView {

    public static LotteryEventAnonymousResponse empty() {
        return LotteryEventAnonymousResponse.builder()
                .build();
    }

    public static LotteryEventAnonymousResponse of(LotteryEvent lotteryEvent) {
        return LotteryEventAnonymousResponse.builder()
                .prize(LotteryEventResponse.PrizeResponse.from(lotteryEvent.getPrize()))
                .build();
    }
}

