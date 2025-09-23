package com.matzip.place.domain;

/**
 * Place 등록 상태를 나타내는 enum
 */
public enum PlaceStatus {
    PENDING,    // 승인 대기
    APPROVED,   // 승인됨
    REJECTED    // 거부됨
}
