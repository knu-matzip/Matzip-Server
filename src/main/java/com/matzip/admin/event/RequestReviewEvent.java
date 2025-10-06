package com.matzip.admin.event;

import com.matzip.admin.domain.RequestReviewStatus;

public record RequestReviewEvent(Long placeId, RequestReviewStatus reviewStatus) {
}
