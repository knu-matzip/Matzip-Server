package com.matzip.admin.controller.request;

import com.matzip.admin.domain.RequestReviewStatus;

public record PlaceRegisterRequestReviewRequestDto(RequestReviewStatus status, String rejectedReason) {
}
