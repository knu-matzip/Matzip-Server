package com.matzip.lottery.repository;

import com.matzip.lottery.domain.Ticket;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Primary
@Repository
public interface TicketJpaRepository extends TicketRepository, JpaRepository<Ticket, String> {
}
