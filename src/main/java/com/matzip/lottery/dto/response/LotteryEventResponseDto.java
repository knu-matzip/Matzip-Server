package com.matzip.lottery.dto.response;

import com.matzip.lottery.domain.LotteryEvent;
import com.matzip.lottery.domain.Prize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record LotteryEventResponseDto(
        @Schema(description = "이벤트 ID", example = "3") Long eventId,
        PrizeResponse prize,
        @Schema(description = "총 당첨자 수", example = "3") int totalWinnersCount,
        @Schema(description = "참여자 수", example = "27") int participantsCount,
        @Schema(description = "사용한 응모권 수", example = "3") int usedTicketsCount,
        @Schema(description = "이벤트 종료 일시", example = "2025-08-21T00:00:00") LocalDateTime eventEndDate)
        implements LotteryEventView {

    public static LotteryEventResponseDto empty() {
        return LotteryEventResponseDto.builder()
                .build();
    }

    public static LotteryEventResponseDto of(LotteryEvent lotteryEvent, int participantsCount, int usedTicketsCount) {
        return LotteryEventResponseDto.builder()
                .eventId(lotteryEvent.getId())
                .prize(PrizeResponse.from(lotteryEvent.getPrize()))
                .totalWinnersCount(lotteryEvent.getWinnersCount())
                .participantsCount(participantsCount)
                .usedTicketsCount(usedTicketsCount)
                .eventEndDate(lotteryEvent.getEndDateTime())
                .build();
    }

    @Builder
    record PrizeResponse(
            @Schema(description = "경품 설명", example = "BHC 뿌링클 치킨 기프티콘 1장") String description,
            @Schema(description = "경품 이미지 URL", example = "https://example.com/images/bhc_bburinkle_chicken.png") String imageUrl) {

        public static PrizeResponse from(Prize prize) {
            return PrizeResponse.builder()
                    .description(prize.getDescription())
                    .imageUrl(prize.getImageUrl())
                    .build();
        }
    }
}
