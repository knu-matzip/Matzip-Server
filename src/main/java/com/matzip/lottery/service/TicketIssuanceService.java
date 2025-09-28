package com.matzip.lottery.service;

import com.matzip.lottery.domain.Ticket;
import com.matzip.lottery.repository.TicketRepository;
import com.matzip.place.domain.Place;
import com.matzip.place.domain.PlaceStatus;
import com.matzip.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class TicketIssuanceService {

    private final TicketRepository ticketRepository;

    public TicketIssuanceService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Transactional
    public Ticket issueTicket(User user, Place place) {
        validatePlaceRegistrationAccepted(place);
        validateRegistrant(user, place);
        validateAlreadyIssued(user, place);

        Ticket issuedTicket = Ticket.issue(user, place);
        ticketRepository.save(issuedTicket);
        return issuedTicket;
    }

    private void validatePlaceRegistrationAccepted(Place place) {
        PlaceStatus placeStatus = place.getStatus();
        if (placeStatus != PlaceStatus.APPROVED) {
            throw new IllegalStateException("등록 요청이 승인된 장소가 아닙니다.");
        }
    }

    private void validateRegistrant(User user, Place place) {
        User registrant = place.getRegisteredBy();
        if (!Objects.equals(registrant.getId(), user.getId())) {
            throw new IllegalStateException("등록자만 응모권을 발급받을 수 있습니다.");
        }
    }

    private void validateAlreadyIssued(User user, Place place) {
        ticketRepository.findByUserIdAndPlaceId(user.getId(), place.getId())
                .ifPresent(ticket -> {
                    throw new IllegalStateException("이미 응모권 발급이 완료되었습니다.");
                });
    }
}
