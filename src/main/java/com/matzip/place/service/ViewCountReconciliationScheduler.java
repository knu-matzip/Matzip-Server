package com.matzip.place.service;

import com.matzip.common.config.ViewCountReconciliationProperties;
import com.matzip.common.infra.discord.DiscordWebhookSender;
import com.matzip.place.domain.entity.ViewCountReconciliation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * 매일 새벽 전날까지의 조회수를 검증 보정하고 보존 기간이 지난 원본 로그를 정리한다.
 * 보정 자체는 ViewCountReconciliationService가 날짜별 트랜잭션으로 수행한다(self-invocation 회피용 분리).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ViewCountReconciliationScheduler {

    private final ViewCountReconciliationService viewCountReconciliationService;
    private final ViewCountReconciliationProperties properties;
    private final DiscordWebhookSender discordWebhookSender;

    // zone은 실행 시각만 정한다. 날짜 계산은 조회 로그의 view_date와 맞추려고 LocalDate.now()를 그대로 쓴다
    @Scheduled(cron = "${app.view-count.reconciliation.cron:0 0 4 * * *}", zone = "Asia/Seoul")
    public void run() {
        reconcileUntil(LocalDate.now());
    }

    void reconcileUntil(LocalDate today) {
        LocalDate yesterday = today.minusDays(1);
        LocalDate oldestRetained = today.minusDays(properties.getRetentionDays());

        // 이력이 없으면 어제만 처리한다. 로그 도입 이전 날짜를 보정하면 집계가 0으로 덮이기 때문이다
        LocalDate from = viewCountReconciliationService.findLastReconciledDate()
                .map(last -> last.plusDays(1))
                .orElse(yesterday);
        if (from.isBefore(oldestRetained)) {
            from = oldestRetained;
        }

        for (LocalDate date = from; !date.isAfter(yesterday); date = date.plusDays(1)) {
            reconcileSafely(date);
        }

        try {
            int purged = viewCountReconciliationService.purgeLogsBefore(oldestRetained);
            log.info("[조회수 검증 보정] {} 이전 원본 로그 {}건 삭제", oldestRetained, purged);
        } catch (Exception e) {
            log.error("[조회수 검증 보정] 원본 로그 정리 실패. before={}", oldestRetained, e);
        }
    }

    private void reconcileSafely(LocalDate date) {
        try {
            ViewCountReconciliation result = viewCountReconciliationService.reconcile(date);
            log.info("[조회수 검증 보정] date={}, logged={}, aggregated={}, missing={}, over={}",
                    date, result.getLoggedCount(), result.getAggregatedCount(),
                    result.getMissingCount(), result.getOverCount());
            if (needsAlert(result)) {
                notifyMismatch(result);
            }
        } catch (Exception e) {
            log.error("[조회수 검증 보정] 실패. date={}", date, e);
            discordWebhookSender.sendAsync("""
                    **조회수 검증 보정 실패**
                    - 날짜: %s
                    - 오류: %s
                    """.formatted(date, e.getMessage()));
        }
    }

    private boolean needsAlert(ViewCountReconciliation result) {
        return result.lossRate() > properties.getAlertLossRateThreshold() || result.getOverCount() > 0;
    }

    private void notifyMismatch(ViewCountReconciliation result) {
        discordWebhookSender.sendAsync("""
                **조회수 검증 보정 결과**
                - 날짜: %s
                - 원본 로그: %d건
                - 보정 전 집계: %d건
                - 누락: %d건 (%.2f%%)
                - 과집계: %d건
                """.formatted(result.getTargetDate(), result.getLoggedCount(), result.getAggregatedCount(),
                result.getMissingCount(), result.lossRate() * 100, result.getOverCount()));
    }
}
