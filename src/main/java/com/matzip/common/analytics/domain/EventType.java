package com.matzip.common.analytics.domain;

/**
 * 서버사이드에서 적재하는 비즈니스 이벤트 종류
 * GA4가 보지 못하거나 DB 조인이 필요한 이벤트만 남긴다.
 */
public enum EventType {
    SIGNUP,
    LOGIN,
    PLACE_REGISTER_REQUESTED,
    PLACE_APPROVED,
    PLACE_REJECTED,
    EVENT_ENTERED
}
