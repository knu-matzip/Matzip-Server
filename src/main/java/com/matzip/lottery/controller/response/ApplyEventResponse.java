package com.matzip.lottery.controller.response;

import com.matzip.lottery.domain.WinnerContact;
import lombok.Builder;


@Builder
public record ApplyEventResponse(
        String phoneNumber,
        AgreementsResponse agreements
) {

    public record AgreementsResponse(boolean termsAgreed, boolean privacyAgreed) {
    }

    public static ApplyEventResponse from(WinnerContact contact) {
        return ApplyEventResponse.builder()
                .phoneNumber(contact.getPhoneNumber())
                .agreements(new AgreementsResponse(contact.isTermsAgreed(), contact.isPrivacyAgreed()))
                .build();
    }
}
