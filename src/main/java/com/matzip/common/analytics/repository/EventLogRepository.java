package com.matzip.common.analytics.repository;

import com.matzip.common.analytics.domain.entity.EventLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventLogRepository extends JpaRepository<EventLog, Long> {
}
