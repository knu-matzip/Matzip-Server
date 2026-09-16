package com.matzip.lottery.dto.response;

import com.matzip.lottery.domain.LotteryEvent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record EventEntryResultResponseDto(
        @Schema(description = "이벤트 ID", example = "3") Long eventId,
        LotteryEventResponseDto.PrizeResponse prize,
        @Schema(description = "총 당첨자 수", example = "3") int totalWinnersCount,
        @Schema(description = "참여자 수", example = "27") int participantsCount,
        @Schema(description = "사용한 응모권 수", example = "3") int usedTicketsCount,
        @Schema(description = "당첨 여부", example = "true") boolean isWinner,
        @Schema(description = "이벤트 종료 일시", example = "2025-08-21T00:00:00") LocalDateTime eventEndDate,
        @Schema(description = "연락처 제출 여부", example = "true") boolean isPhoneSubmitted
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
