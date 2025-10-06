package com.matzip.admin.controller.request;

import com.matzip.admin.domain.RequestReviewStatus;

public record PlaceRegisterRequestReviewRequest(RequestReviewStatus status, String rejectedReason) {
}
