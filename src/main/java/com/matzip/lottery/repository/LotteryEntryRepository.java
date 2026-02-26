package com.matzip.lottery.repository;

import com.matzip.lottery.domain.LotteryEntry;
import com.matzip.lottery.domain.LotteryEvent;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LotteryEntryRepository extends JpaRepository<LotteryEntry, Long> {

    @EntityGraph(attributePaths = {"ticket"})
    List<LotteryEntry> findByLotteryEvent(LotteryEvent lotteryEvent);

    @Query("SELECT DISTINCT le.lotteryEvent FROM LotteryEntry le WHERE le.ticket.userId = :userId ORDER BY le.lotteryEvent.endDateTime DESC")
    List<LotteryEvent> findDistinctLotteryEventsByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(DISTINCT e.ticket.userId) FROM LotteryEntry e WHERE e.lotteryEvent = :event")
    int countParticipantsByLotteryEvent(@Param("event") LotteryEvent event);
}
