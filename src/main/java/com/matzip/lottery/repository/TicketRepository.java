package com.matzip.lottery.repository;

import com.matzip.lottery.domain.Ticket;

import java.util.Optional;

public interface TicketRepository {

    Ticket save(Ticket ticket);

    Optional<Ticket> findByUserIdAndPlaceId(Long UserId, Long placeId);
}
