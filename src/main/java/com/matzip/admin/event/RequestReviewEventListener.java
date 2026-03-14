package com.matzip.admin.event;

import com.matzip.common.exception.BusinessException;
import com.matzip.common.exception.code.ErrorCode;
import com.matzip.lottery.service.LotteryEventService;
import com.matzip.place.domain.entity.Place;
import com.matzip.place.infra.repository.PlaceRepository;
import com.matzip.user.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class RequestReviewEventListener {

    private final LotteryEventService lotteryEventService;
    private final PlaceRepository placeRepository;

    public RequestReviewEventListener(LotteryEventService lotteryEventService, PlaceRepository placeRepository) {
        this.lotteryEventService = lotteryEventService;
        this.placeRepository = placeRepository;
    }

    @Async
    @TransactionalEventListener(condition = "#event.reviewStatus().name() == 'APPROVED'")
    public void enterCurrentEventOnApproval(RequestReviewEvent event) {
        Place place = placeRepository.findById(event.placeId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_FOUND));
        User registrant = place.getRegisteredBy();
        if (registrant == null) {
            return;
        }

        boolean entered = lotteryEventService.enterCurrentEventOnPlaceApproval(registrant.getId(), place.getId());
        if (entered) {
            log.info("[자동 응모 완료] userId: {}, placeId: {}", registrant.getId(), place.getId());
            return;
        }
        log.info("[자동 응모 스킵] 진행 중 이벤트 없음 또는 이미 응모됨. userId: {}, placeId: {}", registrant.getId(), place.getId());
    }
}
