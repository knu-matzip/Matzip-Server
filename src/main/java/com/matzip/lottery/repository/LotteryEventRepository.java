package com.matzip.lottery.repository;

import com.matzip.lottery.domain.LotteryEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface LotteryEventRepository extends JpaRepository<LotteryEvent, Long> {

    @Query("SELECT le FROM LotteryEvent le WHERE le.endDate >= :currentDateTime ORDER BY le.endDate LIMIT 1")
    Optional<LotteryEvent> findCurrentEvent(@Param("currentDateTime") LocalDateTime currentDateTime);
}
