package com.matzip.admin.service;

import com.matzip.admin.domain.RequestReview;
import com.matzip.admin.domain.RequestReviewStatus;
import com.matzip.admin.event.RequestReviewEvent;
import com.matzip.admin.repository.RequestReviewRepository;
import com.matzip.common.exception.BusinessException;
import com.matzip.common.exception.code.ErrorCode;
import com.matzip.place.domain.Place;
import com.matzip.place.domain.PlaceStatus;
import com.matzip.place.infra.repository.PlaceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminPlaceRegisterRequestService {

    private final PlaceRepository placeRepository;
    private final RequestReviewRepository requestReviewRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public AdminPlaceRegisterRequestService(PlaceRepository placeRepository, RequestReviewRepository requestReviewRepository, ApplicationEventPublisher applicationEventPublisher) {
        this.placeRepository = placeRepository;
        this.requestReviewRepository = requestReviewRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional
    public void review(Long placeId, RequestReviewStatus status, String rejectedReason, Long adminId) {
        validateReviewRequest(status, rejectedReason);

        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_FOUND));
        validatePlaceStatus(place);

        switch (status) {
            case APPROVED -> approve(place, adminId);
            case REJECTED -> reject(place, rejectedReason, adminId);
        }

        applicationEventPublisher.publishEvent(new RequestReviewEvent(placeId, status));
    }

    private void approve(Place place, Long adminId) {
        place.approve();
        RequestReview approved = RequestReview.approved(place.getId(), adminId);
        requestReviewRepository.save(approved);
    }

    private void reject(Place place, String rejectedReason, Long adminId) {
        place.reject();
        RequestReview rejected = RequestReview.rejected(place.getId(), adminId, rejectedReason);
        requestReviewRepository.save(rejected);
    }

    private void validateReviewRequest(RequestReviewStatus status, String rejectedReason) {
        if (status == RequestReviewStatus.REJECTED && rejectedReason == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "REJECTED인 경우 거절 사유는 필수로 입력해야 합니다.");
        }
    }

    private void validatePlaceStatus(Place place) {
        if (place.getStatus() != PlaceStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "PENDING 상태의 맛집만 승인/거절 처리가 가능합니다.");
        }
    }
}
