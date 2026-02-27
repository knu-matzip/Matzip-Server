package com.matzip.lottery.controller.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;


public record ApplyEventRequest(
        @NotBlank(message = "핸드폰 번호를 입력해 주세요.")
        @Pattern(regexp = "^010-\\d{3,4}-\\d{4}$", message = "올바른 핸드폰 번호 형식이 아닙니다. (예: 010-1234-5678)")
        String phoneNumber,

        @NotNull(message = "약관 동의 정보가 필요합니다.")
        @Valid
        AgreementsRequest agreements
) {

    public record AgreementsRequest(
            boolean termsAgreed,
            boolean privacyAgreed
    ) {
    }

    public boolean isAllAgreed() {
        return agreements != null && agreements.termsAgreed() && agreements.privacyAgreed();
    }
}
