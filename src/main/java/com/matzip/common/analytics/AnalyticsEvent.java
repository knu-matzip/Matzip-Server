package com.matzip.common.analytics;

import com.matzip.common.analytics.domain.EventType;
import com.matzip.common.analytics.domain.TargetType;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 도메인 서비스가 발행하는 분석 이벤트 페이로드
 * 도메인은 이 record만 발행하고, 적재 책임은 AnalyticsEventListener가 진다
 */
public record AnalyticsEvent(
        EventType eventType,
        Long userId,
        String anonymousId,
        TargetType targetType,
        Long targetId,
        Map<String, Object> properties,
        LocalDateTime occurredAt
) {

    public static AnalyticsEvent of(EventType eventType, Long userId) {
        return new AnalyticsEvent(eventType, userId, null, null, null, null, LocalDateTime.now());
    }

    public static AnalyticsEvent of(EventType eventType, Long userId, TargetType targetType, Long targetId) {
        return new AnalyticsEvent(eventType, userId, null, targetType, targetId, null, LocalDateTime.now());
    }
}
