package com.matzip.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.view-count.reconciliation")
public class ViewCountReconciliationProperties {

    private int retentionDays = 60;

    private double alertLossRateThreshold = 0.01;
}
