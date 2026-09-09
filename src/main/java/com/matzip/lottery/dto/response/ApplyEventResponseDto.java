package com.matzip.lottery.dto.response;

import com.matzip.lottery.domain.WinnerContact;
import lombok.Builder;


@Builder
public record ApplyEventResponseDto(
        String phoneNumber,
        AgreementsResponse agreements
) {

    public record AgreementsResponse(boolean termsAgreed, boolean privacyAgreed) {
    }

    public static ApplyEventResponseDto from(WinnerContact contact) {
        return ApplyEventResponseDto.builder()
                .phoneNumber(contact.getPhoneNumber())
                .agreements(new AgreementsResponse(contact.isTermsAgreed(), contact.isPrivacyAgreed()))
                .build();
    }
}
