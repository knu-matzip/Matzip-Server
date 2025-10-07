package com.matzip.lottery.controller.response;

import com.matzip.lottery.domain.LotteryEvent;
import com.matzip.lottery.domain.Prize;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record LotteryEventResponse(Long eventId, PrizeResponse prize, int totalWinnersCount, int participantsCount,
                                   int usedTicketsCount, int remainingTicketsCount, LocalDateTime eventEndDate) {

    public static LotteryEventResponse empty() {
        return LotteryEventResponse.builder()
                .build();
    }

    public static LotteryEventResponse of(LotteryEvent lotteryEvent, int participantsCount, int usedTicketsCount, int remainingTicketsCount) {
        return LotteryEventResponse.builder()
                .eventId(lotteryEvent.getId())
                .prize(PrizeResponse.from(lotteryEvent.getPrize()))
                .totalWinnersCount(lotteryEvent.getWinnersCount())
                .participantsCount(participantsCount)
                .usedTicketsCount(usedTicketsCount)
                .remainingTicketsCount(remainingTicketsCount)
                .eventEndDate(lotteryEvent.getEndDate())
                .build();
    }

    @Builder
    record PrizeResponse(String description, String imageUrl) {

        public static PrizeResponse from(Prize prize) {
            return PrizeResponse.builder()
                    .description(prize.getDescription())
                    .imageUrl(prize.getImageUrl())
                    .build();
        }
    }
}
