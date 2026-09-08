package com.matzip.common.analytics;

import static org.assertj.core.api.Assertions.assertThat;

import com.matzip.AbstractMatzipApplicationTest;
import com.matzip.common.analytics.domain.EventType;
import com.matzip.common.analytics.repository.EventLogRepository;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 발행(AnalyticsRecorder) → 트랜잭션 커밋 → 비동기 리스너 적재까지의 통합 동작 검증.
 * AFTER_COMMIT 의미상, 커밋된 트랜잭션의 이벤트만 적재되고 롤백되면 남지 않아야 한다.
 */
class AnalyticsEventLoggingIntegrationTest extends AbstractMatzipApplicationTest {

    @Autowired
    private AnalyticsRecorder analyticsRecorder;
    @Autowired
    private EventLogRepository eventLogRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUpTransactionTemplate() {
        transactionTemplate = new TransactionTemplate(transactionManager);
        eventLogRepository.deleteAll();
    }

    @Test
    void 커밋된_트랜잭션에서_발행한_이벤트는_비동기로_적재된다() {
        transactionTemplate.executeWithoutResult(status ->
                analyticsRecorder.record(EventType.LOGIN, 100L));

        // 비동기 AFTER_COMMIT 리스너가 적재할 때까지 대기
        boolean saved = waitUntil(() -> eventLogRepository.count() == 1, 2000);

        assertThat(saved).isTrue();
        assertThat(eventLogRepository.findAll().get(0).getEventType()).isEqualTo(EventType.LOGIN);
    }

    @Test
    void 롤백된_트랜잭션에서_발행한_이벤트는_적재되지_않는다() {
        try {
            transactionTemplate.executeWithoutResult(status -> {
                analyticsRecorder.record(EventType.LOGIN, 200L);
                throw new RuntimeException("의도적 롤백");
            });
        } catch (RuntimeException ignored) {
            // 롤백 유도용 예외
        }

        // AFTER_COMMIT은 롤백 시 실행되지 않으므로, 잠시 대기해도 적재되지 않아야 한다
        boolean neverSaved = stayFalse(() -> eventLogRepository.count() > 0, 500);
        assertThat(neverSaved).isTrue();
    }

    private boolean waitUntil(BooleanSupplier condition, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (condition.getAsBoolean()) {
                return true;
            }
            sleep();
        }
        return condition.getAsBoolean();
    }

    private boolean stayFalse(BooleanSupplier condition, long durationMs) {
        long deadline = System.currentTimeMillis() + durationMs;
        while (System.currentTimeMillis() < deadline) {
            if (condition.getAsBoolean()) {
                return false;
            }
            sleep();
        }
        return !condition.getAsBoolean();
    }

    private void sleep() {
        try {
            TimeUnit.MILLISECONDS.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
