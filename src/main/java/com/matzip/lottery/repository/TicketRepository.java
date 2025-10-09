package com.matzip.lottery.repository;

import com.matzip.lottery.domain.Ticket;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface TicketRepository {

    Ticket save(Ticket ticket);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Ticket> findByUserIdAndPlaceId(Long userId, Long placeId);

    int countByUserIdAndStatus(Long userId, Ticket.Status status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Ticket> findByUserIdAndStatus(Long userId, Ticket.Status status, Sort sort);
}
