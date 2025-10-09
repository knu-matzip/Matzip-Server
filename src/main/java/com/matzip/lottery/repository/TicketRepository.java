package com.matzip.lottery.repository;

import com.matzip.lottery.domain.Ticket;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface TicketRepository {

    Ticket save(Ticket ticket);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Ticket> findByUserIdAndPlaceId(Long userId, Long placeId);

    int countByUserIdAndStatus(Long userId, Ticket.Status status);
}
