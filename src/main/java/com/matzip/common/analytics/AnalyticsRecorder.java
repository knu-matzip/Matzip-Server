package com.matzip.common.analytics;

import com.matzip.common.analytics.domain.EventType;
import com.matzip.common.analytics.domain.TargetType;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 도메인 서비스가 한 줄로 분석 이벤트를 발행하도록 돕는 얇은 헬퍼
 * 실제 적재는 AnalyticsEventListener가 커밋 이후 비동기로 수행한다.
 */
@Component
public class AnalyticsRecorder {

    private final ApplicationEventPublisher applicationEventPublisher;

    public AnalyticsRecorder(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void record(AnalyticsEvent event) {
        applicationEventPublisher.publishEvent(event);
    }

    public void record(EventType eventType, Long userId) {
        record(AnalyticsEvent.of(eventType, userId));
    }

    public void record(EventType eventType, Long userId, TargetType targetType, Long targetId) {
        record(AnalyticsEvent.of(eventType, userId, targetType, targetId));
    }
}
