package com.matzip.common.analytics;

import com.matzip.common.analytics.domain.entity.EventLog;
import com.matzip.common.analytics.repository.EventLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 분석 이벤트를 event_log에 적재한다.
 * - AFTER_COMMIT: 커밋된 액션만 기록(롤백되면 남기지 않음)
 * - fallbackExecution: 트랜잭션 밖에서 발행된 이벤트(예: 자동 응모 후)도 놓치지 않음
 * - @Async + try-catch: 적재 실패·지연이 비즈니스 흐름에 영향을 주지 않도록 격리(fire-and-forget)
 */
@Slf4j
@Component
public class AnalyticsEventListener {

    private final EventLogRepository eventLogRepository;

    public AnalyticsEventListener(EventLogRepository eventLogRepository) {
        this.eventLogRepository = eventLogRepository;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(AnalyticsEvent event) {
        try {
            eventLogRepository.save(EventLog.from(event));
        } catch (Exception e) {
            log.warn("[분석 이벤트 적재 실패] type={}, userId={}, targetId={}",
                    event.eventType(), event.userId(), event.targetId(), e);
        }
    }
}
