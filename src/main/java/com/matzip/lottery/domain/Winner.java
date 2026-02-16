package com.matzip.lottery.domain;

import com.matzip.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Builder;
import lombok.Getter;

@Getter
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"userId", "eventId"})
})
@Entity
public class Winner extends BaseEntity {

    private Long userId;

    private Long eventId;

    protected Winner() {
    }

    @Builder
    public Winner(Long id, Long userId, Long eventId) {
        super(id);
        this.userId = userId;
        this.eventId = eventId;
    }
}
