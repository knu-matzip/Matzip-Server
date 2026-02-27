package com.matzip.lottery.repository;

import com.matzip.lottery.domain.WinnerContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WinnerContactRepository extends JpaRepository<WinnerContact, Long> {

    Optional<WinnerContact> findByUserIdAndEventId(Long userId, Long eventId);
}
