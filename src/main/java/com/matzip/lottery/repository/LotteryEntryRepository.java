package com.matzip.lottery.repository;

import com.matzip.lottery.domain.LotteryEntry;
import com.matzip.lottery.domain.LotteryEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LotteryEntryRepository extends JpaRepository<LotteryEntry, Long> {

    List<LotteryEntry> findByLotteryEvent(LotteryEvent lotteryEvent);

    @Query("SELECT DISTINCT le.lotteryEvent FROM LotteryEntry le WHERE le.userId = :userId ORDER BY le.lotteryEvent.endDateTime DESC")
    List<LotteryEvent> findDistinctLotteryEventsByUserId(@Param("userId") Long userId);

    @Query("SELECT DISTINCT le.lotteryEvent FROM LotteryEntry le WHERE le.userId = :userId AND le.lotteryEvent.endDateTime < :currentDateTime ORDER BY le.lotteryEvent.endDateTime DESC")
    List<LotteryEvent> findDistinctEndedLotteryEventsByUserId(@Param("userId") Long userId, @Param("currentDateTime") LocalDateTime currentDateTime);

    @Query("SELECT COUNT(DISTINCT e.userId) FROM LotteryEntry e WHERE e.lotteryEvent = :event")
    int countParticipantsByLotteryEvent(@Param("event") LotteryEvent event);

    @Query("SELECT COUNT(le) FROM LotteryEntry le WHERE le.lotteryEvent.id = :eventId AND le.userId = :userId")
    int countByLotteryEventIdAndUserId(@Param("eventId") Long eventId, @Param("userId") Long userId);

    boolean existsByLotteryEvent_IdAndPlaceId(Long lotteryEventId, Long placeId);
}
