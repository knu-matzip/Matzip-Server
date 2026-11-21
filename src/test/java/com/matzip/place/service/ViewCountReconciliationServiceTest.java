package com.matzip.place.service;

import com.matzip.fixture.PlaceFixtures;
import com.matzip.place.domain.DailyViewCount;
import com.matzip.place.domain.entity.Place;
import com.matzip.place.domain.entity.PlaceViewLog;
import com.matzip.place.domain.entity.ViewCountReconciliation;
import com.matzip.place.repository.DailyViewCountRepository;
import com.matzip.place.repository.PlaceRepository;
import com.matzip.place.repository.PlaceViewLogRepository;
import com.matzip.place.repository.ViewCountReconciliationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ViewCountReconciliationServiceTest {

    // 다른 테스트가 만드는 오늘/어제 데이터와 섞이지 않도록 고정 날짜를 쓴다
    private static final LocalDate DATE = LocalDate.of(2026, 1, 15);

    @Autowired
    private ViewCountReconciliationService viewCountReconciliationService;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private PlaceViewLogRepository placeViewLogRepository;

    @Autowired
    private DailyViewCountRepository dailyViewCountRepository;

    @Autowired
    private ViewCountReconciliationRepository viewCountReconciliationRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @BeforeEach
    void setUp() {
        viewCountReconciliationRepository.deleteAll();
        placeViewLogRepository.deleteAll();
        dailyViewCountRepository.deleteAll();
    }

    @Test
    void 집계가_로그보다_적으면_로그_기준으로_보정하고_누락으로_기록한다() {
        Place place = createPlaceWithViewCount(2);
        saveLogs(place, DATE, 5);
        saveDaily(place, DATE, 2);

        ViewCountReconciliation result = viewCountReconciliationService.reconcile(DATE);

        assertThat(result.getLoggedCount()).isEqualTo(5);
        assertThat(result.getAggregatedCount()).isEqualTo(2);
        assertThat(result.getMissingCount()).isEqualTo(3);
        assertThat(result.getOverCount()).isZero();
        assertThat(result.lossRate()).isEqualTo(0.6);
        assertThat(dailyCountsOf(place)).containsExactly(5);
        assertThat(viewCountOf(place)).isEqualTo(5);
    }

    @Test
    void 집계가_로그보다_많으면_로그_기준으로_줄이고_과집계로_기록한다() {
        Place place = createPlaceWithViewCount(10);
        saveLogs(place, DATE, 3);
        saveDaily(place, DATE, 4);

        ViewCountReconciliation result = viewCountReconciliationService.reconcile(DATE);

        assertThat(result.getMissingCount()).isZero();
        assertThat(result.getOverCount()).isEqualTo(1);
        assertThat(dailyCountsOf(place)).containsExactly(3);
        assertThat(viewCountOf(place)).isEqualTo(9);
    }

    @Test
    void 같은_날짜의_중복_집계_행은_합산해_비교하고_한_행으로_정리한다() {
        Place place = createPlaceWithViewCount(4);
        saveLogs(place, DATE, 4);
        saveDaily(place, DATE, 1);
        saveDaily(place, DATE, 3);

        ViewCountReconciliation result = viewCountReconciliationService.reconcile(DATE);

        assertThat(result.getMissingCount()).isZero();
        assertThat(result.getOverCount()).isZero();
        assertThat(dailyCountsOf(place)).containsExactly(4);
        assertThat(viewCountOf(place)).isEqualTo(4);
    }

    @Test
    void 로그가_없는_맛집의_집계는_삭제되고_누적에서_빠진다() {
        Place place = createPlaceWithViewCount(7);
        saveDaily(place, DATE, 2);

        ViewCountReconciliation result = viewCountReconciliationService.reconcile(DATE);

        assertThat(result.getOverCount()).isEqualTo(2);
        assertThat(dailyCountsOf(place)).isEmpty();
        assertThat(viewCountOf(place)).isEqualTo(5);
    }

    @Test
    void 다른_날짜의_집계는_건드리지_않는다() {
        Place place = createPlaceWithViewCount(0);
        saveDaily(place, DATE.plusDays(1), 9);

        viewCountReconciliationService.reconcile(DATE);

        assertThat(dailyCountsOf(place)).containsExactly(9);
    }

    @Test
    void 보정한_날짜를_다시_보정해도_값이_바뀌지_않는다() {
        Place place = createPlaceWithViewCount(1);
        saveLogs(place, DATE, 3);
        saveDaily(place, DATE, 1);
        viewCountReconciliationService.reconcile(DATE);
        viewCountReconciliationRepository.deleteAll();

        ViewCountReconciliation second = viewCountReconciliationService.reconcile(DATE);

        assertThat(second.getMissingCount()).isZero();
        assertThat(second.getOverCount()).isZero();
        assertThat(dailyCountsOf(place)).containsExactly(3);
        assertThat(viewCountOf(place)).isEqualTo(3);
    }

    @Test
    void 마지막_검증_날짜를_조회한다() {
        assertThat(viewCountReconciliationService.findLastReconciledDate()).isEmpty();

        viewCountReconciliationService.reconcile(DATE);
        viewCountReconciliationService.reconcile(DATE.plusDays(1));

        assertThat(viewCountReconciliationService.findLastReconciledDate()).contains(DATE.plusDays(1));
    }

    @Test
    void 기준일_이전의_원본_로그만_삭제한다() {
        Place place = createPlaceWithViewCount(0);
        saveLogs(place, DATE.minusDays(1), 2);
        saveLogs(place, DATE, 1);

        int purged = viewCountReconciliationService.purgeLogsBefore(DATE);

        assertThat(purged).isEqualTo(2);
        assertThat(placeViewLogRepository.findAll())
                .extracting(PlaceViewLog::getViewDate)
                .containsExactly(DATE);
    }

    private Place createPlaceWithViewCount(int viewCount) {
        Place place = placeRepository.save(PlaceFixtures.createPlaceWith(UUID.randomUUID().toString(), null));
        if (viewCount > 0) {
            new TransactionTemplate(transactionManager).executeWithoutResult(status ->
                    placeRepository.addViewCount(place.getId(), viewCount));
        }
        return place;
    }

    private void saveLogs(Place place, LocalDate date, int times) {
        for (int i = 0; i < times; i++) {
            placeViewLogRepository.save(PlaceViewLog.of(place.getId(), null, date));
        }
    }

    private void saveDaily(Place place, LocalDate date, int count) {
        dailyViewCountRepository.save(new DailyViewCount(place, date, count));
    }

    private List<Integer> dailyCountsOf(Place place) {
        return dailyViewCountRepository.findAll().stream()
                .filter(daily -> daily.getPlace().getId().equals(place.getId()))
                .map(DailyViewCount::getCount)
                .toList();
    }

    private int viewCountOf(Place place) {
        return placeRepository.findById(place.getId()).orElseThrow().getViewCount();
    }
}
