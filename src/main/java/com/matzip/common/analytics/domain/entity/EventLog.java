package com.matzip.common.analytics.domain.entity;

import com.matzip.common.analytics.AnalyticsEvent;
import com.matzip.common.analytics.JsonMapConverter;
import com.matzip.common.analytics.domain.EventType;
import com.matzip.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Entity
@Table(
        name = "event_log",
        indexes = {
                @Index(name = "idx_event_log_type_occurred", columnList = "event_type, occurred_at"),
                @Index(name = "idx_event_log_user_occurred", columnList = "user_id, occurred_at")
        }
)
public class EventLog extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private EventType eventType;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "anonymous_id", length = 100)
    private String anonymousId;

    @Column(name = "target_type", length = 50)
    private String targetType;

    @Column(name = "target_id")
    private Long targetId;

    @Convert(converter = JsonMapConverter.class)
    @Column(name = "properties", length = 4000)
    private Map<String, Object> properties;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    protected EventLog() {
    }

    @Builder
    private EventLog(EventType eventType, Long userId, String anonymousId, String targetType,
                     Long targetId, Map<String, Object> properties, LocalDateTime occurredAt) {
        this.eventType = eventType;
        this.userId = userId;
        this.anonymousId = anonymousId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.properties = properties;
        this.occurredAt = occurredAt;
    }

    public static EventLog from(AnalyticsEvent event) {
        return EventLog.builder()
                .eventType(event.eventType())
                .userId(event.userId())
                .anonymousId(event.anonymousId())
                .targetType(event.targetType() == null ? null : event.targetType().name())
                .targetId(event.targetId())
                .properties(event.properties())
                .occurredAt(event.occurredAt() == null ? LocalDateTime.now() : event.occurredAt())
                .build();
    }
}
