package com.matzip.admin.dto.request;

import com.matzip.admin.domain.RequestReviewStatus;

public record PlaceRegisterRequestReviewRequestDto(RequestReviewStatus status, String rejectedReason) {
}
