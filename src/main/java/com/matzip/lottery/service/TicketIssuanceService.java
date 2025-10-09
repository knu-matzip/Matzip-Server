package com.matzip.lottery.service;

import com.matzip.lottery.domain.Ticket;
import com.matzip.lottery.repository.TicketRepository;
import com.matzip.place.domain.PlaceStatus;
import com.matzip.place.domain.entity.Place;
import com.matzip.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketIssuanceService {

    private final TicketRepository ticketRepository;

    public TicketIssuanceService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Transactional
    public Ticket issueTicket(User user, Place place) {
        validatePlaceRegistrationAccepted(place);
        validateAlreadyIssued(user, place);

        Ticket issuedTicket = Ticket.issue(user, place);
        ticketRepository.save(issuedTicket);
        // TODO: 필요한 경우 이벤트 발행 로직 추가 (e.g. 응모권 발급 푸시 알림 등)
        return issuedTicket;
    }

    private void validatePlaceRegistrationAccepted(Place place) {
        if (place.getStatus() != PlaceStatus.APPROVED) {
            throw new IllegalStateException("[이벤트 응모권 발급] 맛집 등록 요청이 승인되지 않았습니다.");
        }
    }

    private void validateAlreadyIssued(User user, Place place) {
        ticketRepository.findByUserIdAndPlaceId(user.getId(), place.getId())
                .ifPresent(ticket -> {
                    throw new IllegalStateException("이미 응모권 발급이 완료되었습니다.");
                });
    }
}
