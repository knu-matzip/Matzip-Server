package com.matzip.place.service;

import com.matzip.common.config.ViewCountReconciliationProperties;
import com.matzip.common.infra.discord.DiscordWebhookSender;
import com.matzip.place.domain.entity.ViewCountReconciliation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

// 날짜별 트랜잭션은 self-invocation을 피하려고 ViewCountReconciliationService로 분리했다
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

        // prod는 배포 DDL이 배포일 이력을 넣어 두므로 이 분기를 타지 않는다(sql/view_count_reconciliation.sql)
        LocalDate from = viewCountReconciliationService.findLastReconciledDate()
                .map(last -> last.plusDays(1))
                .orElse(yesterday);
        if (from.isBefore(oldestRetained)) {
            from = oldestRetained;
        }

        // 실패한 날짜를 넘어가면 다음 실행의 시작일(마지막 보정일 + 1)이 그 날짜를 건너뛰므로 멈춘다
        for (LocalDate date = from; !date.isAfter(yesterday); date = date.plusDays(1)) {
            if (!reconcileSafely(date, yesterday)) {
                break;
            }
        }

        try {
            int purged = viewCountReconciliationService.purgeLogsBefore(oldestRetained);
            log.info("[조회수 검증 보정] {} 이전 원본 로그 {}건 삭제", oldestRetained, purged);
        } catch (Exception e) {
            log.error("[조회수 검증 보정] 원본 로그 정리 실패. before={}", oldestRetained, e);
        }
    }

    private boolean reconcileSafely(LocalDate date, LocalDate yesterday) {
        try {
            ViewCountReconciliation result = viewCountReconciliationService.reconcile(date);
            log.info("[조회수 검증 보정] date={}, logged={}, aggregated={}, missing={}, over={}",
                    date, result.getLoggedCount(), result.getAggregatedCount(),
                    result.getMissingCount(), result.getOverCount());
            if (needsAlert(result)) {
                notifyMismatch(result);
            }
            return true;
        } catch (Exception e) {
            log.error("[조회수 검증 보정] 실패. date={}", date, e);
            // 어제보다 이전 날짜는 이전 실행에서 이미 실패한 재시도라 자동 복구되지 않은 것으로 보고 알린다
            if (date.isBefore(yesterday)) {
                discordWebhookSender.sendAsync("""
                        **조회수 검증 보정 재시도 실패**
                        - 날짜: %s
                        - 오류: %s
                        - 이후 날짜는 이 날짜가 보정될 때까지 처리하지 않는다
                        """.formatted(date, e.getMessage()));
            }
            return false;
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
