package com.matzip.lottery.domain;

import com.matzip.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Builder;
import lombok.Getter;

@Getter
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"userId", "eventId"})
})
public class WinnerContact extends BaseEntity {

    private Long userId;

    private Long eventId;

    @Column(nullable = false, length = 20)
    private String phoneNumber;

    @Column(nullable = false)
    private boolean termsAgreed;

    @Column(nullable = false)
    private boolean privacyAgreed;

    protected WinnerContact() {
    }

    @Builder
    public WinnerContact(Long userId, Long eventId, String phoneNumber, boolean termsAgreed, boolean privacyAgreed) {
        this.userId = userId;
        this.eventId = eventId;
        this.phoneNumber = phoneNumber;
        this.termsAgreed = termsAgreed;
        this.privacyAgreed = privacyAgreed;
    }

    public void update(String phoneNumber, boolean termsAgreed, boolean privacyAgreed) {
        this.phoneNumber = phoneNumber;
        this.termsAgreed = termsAgreed;
        this.privacyAgreed = privacyAgreed;
    }
}
