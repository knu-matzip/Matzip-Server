package com.matzip.lottery.repository;

import com.matzip.lottery.domain.Winner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WinnerRepository extends JpaRepository<Winner, Long> {
}
