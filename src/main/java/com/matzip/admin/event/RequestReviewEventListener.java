package com.matzip.admin.event;

import com.matzip.common.exception.BusinessException;
import com.matzip.common.exception.code.ErrorCode;
import com.matzip.lottery.domain.Ticket;
import com.matzip.lottery.service.TicketIssuanceService;
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

    private final TicketIssuanceService ticketIssuanceService;
    private final PlaceRepository placeRepository;

    public RequestReviewEventListener(TicketIssuanceService ticketIssuanceService, PlaceRepository placeRepository) {
        this.ticketIssuanceService = ticketIssuanceService;
        this.placeRepository = placeRepository;
    }

    @Async
    @TransactionalEventListener(condition = "#event.reviewStatus().name() == 'APPROVED'")
    public void issueTicketOnApproval(RequestReviewEvent event) {
        Place place = placeRepository.findById(event.placeId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_FOUND));
        User registrant = place.getRegisteredBy();
        if (registrant == null) {
            return;
        }

        Ticket ticket = ticketIssuanceService.issueTicket(registrant, place);
        log.info("[응모권 발급 완료] userId: {}, placeId: {}", ticket.getUserId(), ticket.getPlaceId());
    }
}
