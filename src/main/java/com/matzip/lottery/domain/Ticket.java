package com.matzip.lottery.domain;

import com.matzip.common.entity.BaseEntity;
import com.matzip.place.domain.Place;
import com.matzip.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 응모권
 */
@Getter
@ToString
@Entity
public class Ticket extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(10)")
    private Status status;

    // TODO: 연관관계 매핑 필요한 경우 추가
    private Long userId;

    // TODO: 연관관계 매핑 필요한 경우 추가
    @Column(unique = true)
    private Long placeId;

    public Ticket() {
    }

    @Builder
    public Ticket(Status status, Long userId, Long placeId) {
        this.status = status;
        this.userId = userId;
        this.placeId = placeId;
    }

    public static Ticket issue(User user, Place place) {
        return Ticket.builder()
                .status(Status.ACTIVE)
                .userId(user.getId())
                .placeId(place.getId())
                .build();
    }

    public LocalDateTime getIssuedAt() {
        return this.getCreatedAt();
    }

    public enum Status {
        ACTIVE, USED
    }
}
