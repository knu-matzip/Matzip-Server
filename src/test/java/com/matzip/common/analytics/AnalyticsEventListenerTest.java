package com.matzip.common.analytics;

import static org.assertj.core.api.Assertions.assertThat;

import com.matzip.AbstractMatzipApplicationTest;
import com.matzip.common.analytics.domain.EventType;
import com.matzip.common.analytics.domain.TargetType;
import com.matzip.common.analytics.domain.entity.EventLog;
import com.matzip.common.analytics.repository.EventLogRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AnalyticsEventListenerTest extends AbstractMatzipApplicationTest {

    @Autowired
    private EventLogRepository eventLogRepository;

    // @Async 프록시를 우회해 매핑·적재 로직을 동기적으로 검증하기 위해 직접 생성한다.
    private AnalyticsEventListener analyticsEventListener;

    @BeforeEach
    void clearEventLog() {
        eventLogRepository.deleteAll();
        analyticsEventListener = new AnalyticsEventListener(eventLogRepository);
    }

    @Test
    void 이벤트를_event_log에_필드까지_그대로_적재한다() {
        AnalyticsEvent event = AnalyticsEvent.of(EventType.PLACE_REGISTER_REQUESTED, 42L, TargetType.PLACE, 7L);

        analyticsEventListener.on(event);

        List<EventLog> logs = eventLogRepository.findAll();
        assertThat(logs).hasSize(1);
        EventLog saved = logs.get(0);
        assertThat(saved.getEventType()).isEqualTo(EventType.PLACE_REGISTER_REQUESTED);
        assertThat(saved.getUserId()).isEqualTo(42L);
        assertThat(saved.getTargetType()).isEqualTo("PLACE");
        assertThat(saved.getTargetId()).isEqualTo(7L);
        assertThat(saved.getOccurredAt()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void properties가_JSON으로_왕복된다() {
        AnalyticsEvent event = new AnalyticsEvent(
                EventType.PLACE_REJECTED, null, null, TargetType.PLACE, 3L,
                Map.of("reason", "중복 등록"), LocalDateTime.now());

        analyticsEventListener.on(event);

        EventLog saved = eventLogRepository.findAll().get(0);
        assertThat(saved.getUserId()).isNull();
        assertThat(saved.getProperties()).containsEntry("reason", "중복 등록");
    }
}
