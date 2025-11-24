package com.matzip.admin.domain;

import com.matzip.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Getter;

@Getter
@Entity
public class RequestReview extends BaseEntity {

    private Long placeId;

    private Long adminId;

    @Enumerated(EnumType.STRING)
    private RequestReviewStatus status;

    private String rejectedReason;

    protected RequestReview() {
    }

    @Builder
    public RequestReview(Long placeId, Long adminId, RequestReviewStatus status, String rejectedReason) {
        this.placeId = placeId;
        this.adminId = adminId;
        this.status = status;
        this.rejectedReason = rejectedReason;
    }

    public static RequestReview approved(Long placeId, Long adminId) {
        return RequestReview.builder()
                .placeId(placeId)
                .adminId(adminId)
                .status(RequestReviewStatus.APPROVED)
                .build();
    }

    public static RequestReview rejected(Long placeId, Long adminId, String rejectedReason) {
        return RequestReview.builder()
                .placeId(placeId)
                .adminId(adminId)
                .status(RequestReviewStatus.REJECTED)
                .rejectedReason(rejectedReason)
                .build();
    }
}
