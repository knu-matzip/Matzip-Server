package com.matzip.place.service;

import com.matzip.common.config.ViewCountReconciliationProperties;
import com.matzip.common.infra.discord.DiscordWebhookSender;
import com.matzip.place.domain.entity.ViewCountReconciliation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ViewCountReconciliationSchedulerTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 3, 10);

    @Mock
    private ViewCountReconciliationService viewCountReconciliationService;

    @Mock
    private DiscordWebhookSender discordWebhookSender;

    private ViewCountReconciliationScheduler scheduler;

    @BeforeEach
    void setUp() {
        ViewCountReconciliationProperties properties = new ViewCountReconciliationProperties();
        properties.setRetentionDays(60);
        properties.setAlertLossRateThreshold(0.01);
        scheduler = new ViewCountReconciliationScheduler(viewCountReconciliationService, properties, discordWebhookSender);
    }

    @Test
    void 검증_이력이_없으면_어제만_처리한다() {
        given(viewCountReconciliationService.findLastReconciledDate()).willReturn(Optional.empty());
        given(viewCountReconciliationService.reconcile(any())).willAnswer(inv -> result(inv.getArgument(0), 100, 0, 0));

        scheduler.reconcileUntil(TODAY);

        verify(viewCountReconciliationService, times(1)).reconcile(any());
        verify(viewCountReconciliationService).reconcile(TODAY.minusDays(1));
        verify(viewCountReconciliationService).purgeLogsBefore(TODAY.minusDays(60));
    }

    @Test
    void 밀린_날짜를_순서대로_처리한다() {
        given(viewCountReconciliationService.findLastReconciledDate()).willReturn(Optional.of(TODAY.minusDays(4)));
        given(viewCountReconciliationService.reconcile(any())).willAnswer(inv -> result(inv.getArgument(0), 100, 0, 0));

        scheduler.reconcileUntil(TODAY);

        InOrder inOrder = inOrder(viewCountReconciliationService);
        inOrder.verify(viewCountReconciliationService).reconcile(TODAY.minusDays(3));
        inOrder.verify(viewCountReconciliationService).reconcile(TODAY.minusDays(2));
        inOrder.verify(viewCountReconciliationService).reconcile(TODAY.minusDays(1));
        verify(viewCountReconciliationService, times(3)).reconcile(any());
    }

    @Test
    void 어제까지_처리했으면_보정하지_않는다() {
        given(viewCountReconciliationService.findLastReconciledDate()).willReturn(Optional.of(TODAY.minusDays(1)));

        scheduler.reconcileUntil(TODAY);

        verify(viewCountReconciliationService, never()).reconcile(any());
        verify(viewCountReconciliationService).purgeLogsBefore(TODAY.minusDays(60));
    }

    @Test
    void 밀린_날짜는_보존_기간_이내로만_처리한다() {
        given(viewCountReconciliationService.findLastReconciledDate()).willReturn(Optional.of(TODAY.minusDays(100)));
        given(viewCountReconciliationService.reconcile(any())).willAnswer(inv -> result(inv.getArgument(0), 100, 0, 0));

        scheduler.reconcileUntil(TODAY);

        verify(viewCountReconciliationService).reconcile(TODAY.minusDays(60));
        verify(viewCountReconciliationService, never()).reconcile(TODAY.minusDays(61));
        verify(viewCountReconciliationService, times(60)).reconcile(any());
    }

    @Test
    void 한_날짜가_실패해도_다음_날짜를_처리하고_실패를_알린다() {
        given(viewCountReconciliationService.findLastReconciledDate()).willReturn(Optional.of(TODAY.minusDays(3)));
        given(viewCountReconciliationService.reconcile(TODAY.minusDays(2))).willThrow(new IllegalStateException("db down"));
        given(viewCountReconciliationService.reconcile(TODAY.minusDays(1))).willReturn(result(TODAY.minusDays(1), 100, 0, 0));

        scheduler.reconcileUntil(TODAY);

        verify(viewCountReconciliationService).reconcile(TODAY.minusDays(1));
        verify(discordWebhookSender).sendAsync(contains("실패"));
    }

    @Test
    void 누락률이_임계치를_넘으면_알린다() {
        given(viewCountReconciliationService.findLastReconciledDate()).willReturn(Optional.empty());
        given(viewCountReconciliationService.reconcile(any())).willReturn(result(TODAY.minusDays(1), 100, 2, 0));

        scheduler.reconcileUntil(TODAY);

        verify(discordWebhookSender).sendAsync(contains("누락: 2건"));
    }

    @Test
    void 누락률이_임계치_이하이고_과집계가_없으면_알리지_않는다() {
        given(viewCountReconciliationService.findLastReconciledDate()).willReturn(Optional.empty());
        given(viewCountReconciliationService.reconcile(any())).willReturn(result(TODAY.minusDays(1), 100, 1, 0));

        scheduler.reconcileUntil(TODAY);

        verify(discordWebhookSender, never()).sendAsync(anyString());
    }

    @Test
    void 과집계가_있으면_알린다() {
        given(viewCountReconciliationService.findLastReconciledDate()).willReturn(Optional.empty());
        given(viewCountReconciliationService.reconcile(any())).willReturn(result(TODAY.minusDays(1), 100, 0, 1));

        scheduler.reconcileUntil(TODAY);

        verify(discordWebhookSender).sendAsync(contains("과집계: 1건"));
    }

    private ViewCountReconciliation result(LocalDate date, long logged, long missing, long over) {
        return new ViewCountReconciliation(date, logged, logged - missing + over, missing, over);
    }
}
