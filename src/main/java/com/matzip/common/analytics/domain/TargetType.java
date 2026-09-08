package com.matzip.common.analytics.domain;

/**
 * 이벤트가 가리키는 대상 종류. target_id와 함께 저장해 비즈니스 테이블과 조인할 때 쓴다.
 */
public enum TargetType {
    PLACE,
    LOTTERY_EVENT
}
