package com.matzip.place.repository;

import com.matzip.place.domain.entity.ViewCountReconciliation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ViewCountReconciliationRepository extends JpaRepository<ViewCountReconciliation, Long> {

    Optional<ViewCountReconciliation> findTopByOrderByTargetDateDesc();
}
