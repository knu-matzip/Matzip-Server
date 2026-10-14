package com.matzip.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.view-count.reconciliation")
public class ViewCountReconciliationProperties {

    // 원본 로그 보존 기간이자 밀린 날짜를 거슬러 처리할 수 있는 최대 범위
    private int retentionDays = 60;

    // 누락률(누락 건수 / 로그 건수)이 이 값을 넘으면 Discord로 알린다
    private double alertLossRateThreshold = 0.01;
}
