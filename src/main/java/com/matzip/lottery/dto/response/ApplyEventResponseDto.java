package com.matzip.lottery.dto.response;

import com.matzip.lottery.domain.WinnerContact;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;


@Builder
public record ApplyEventResponseDto(
        @Schema(description = "연락처", example = "010-1234-5678") String phoneNumber,
        AgreementsResponse agreements
) {

    public record AgreementsResponse(
            @Schema(description = "이용약관 동의 여부", example = "true") boolean termsAgreed,
            @Schema(description = "개인정보 수집 동의 여부", example = "true") boolean privacyAgreed) {
    }

    public static ApplyEventResponseDto from(WinnerContact contact) {
        return ApplyEventResponseDto.builder()
                .phoneNumber(contact.getPhoneNumber())
                .agreements(new AgreementsResponse(contact.isTermsAgreed(), contact.isPrivacyAgreed()))
                .build();
    }
}
