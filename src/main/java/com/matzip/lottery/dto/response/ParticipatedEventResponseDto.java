package com.matzip.lottery.dto.response;

import com.matzip.lottery.domain.LotteryEvent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ParticipatedEventResponseDto(
        @Schema(description = "이벤트 ID", example = "3") Long eventId,
        LotteryEventResponseDto.PrizeResponse prize,
        @Schema(description = "총 당첨자 수", example = "3") int totalWinnersCount,
        @Schema(description = "참여자 수", example = "27") int participantsCount,
        @Schema(description = "이벤트 종료 일시", example = "2025-08-21T00:00:00") LocalDateTime eventEndDate
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
