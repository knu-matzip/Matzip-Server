package com.matzip.lottery.repository;

import com.matzip.lottery.domain.LotteryEntry;
import com.matzip.lottery.domain.LotteryEvent;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LotteryEntryRepository extends JpaRepository<LotteryEntry, Long> {

    @EntityGraph(attributePaths = {"ticket"})
    List<LotteryEntry> findByLotteryEvent(LotteryEvent lotteryEvent);
}
