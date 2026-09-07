package com.matzip.place.application;

import com.matzip.place.service.PlaceReadService;
import com.matzip.place.domain.Campus;
import com.matzip.place.domain.DailyViewCount;
import com.matzip.place.domain.PlaceStatus;
import com.matzip.place.domain.SortType;
import com.matzip.place.domain.entity.Place;
import com.matzip.place.dto.response.PlaceCommonResponseDto;
import com.matzip.place.repository.DailyViewCountRepository;
import com.matzip.place.repository.PlaceRepository;
import com.matzip.user.domain.User;
import com.matzip.user.repository.UserRepository;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Slf4j
@SpringBootTest
public class PlaceReadServiceTest {

    @Autowired
    private PlaceReadService placeReadService;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DailyViewCountRepository dailyViewCountRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private Place testPlace;
    private User testUser;

    @BeforeEach
    void setUp() {
        // 모든 테스트 전에 실행될 초기 데이터 설정
        dailyViewCountRepository.deleteAll();
        placeRepository.deleteAll();
        userRepository.deleteAll();

        testUser = userRepository.save(User.builder().kakaoId(12345L).nickname("test_user").build());
        testPlace = placeRepository.save(Place.builder()
                .name("테스트 맛집")
                .address("테스트 주소")
                .kakaoPlaceId("1234567")
                .campus(Campus.CHEONAN)
                .latitude(36.123)
                .longitude(127.123)
                .status(PlaceStatus.APPROVED)
                .build());
    }

    @Test
    @DisplayName("상세 페이지를 조회하면 조회수가 1 증가한다")
    void getPlaceDetail_incrementsViewCount() {
        // given
        int initialViewCount = placeRepository.findById(testPlace.getId()).get().getViewCount();
        assertThat(initialViewCount).isEqualTo(0);

        // when
        placeReadService.getPlaceDetail(testPlace.getId(), testUser.getId());

        // then
        await().atMost(5, TimeUnit.SECONDS) // 최대 5초 대기
                .untilAsserted(() -> { // 이 조건이 통과될 때까지 반복
                    Place updatedPlace = placeRepository.findById(testPlace.getId()).get();
//                    log.info("[단일] Awaitility 폴링 중... 현재 조회수: {}", updatedPlace.getViewCount());
                    assertThat(updatedPlace.getViewCount()).isEqualTo(1);
                });
    }

    @Test
    @DisplayName("여러 요청이 동시에 들어와도 조회수가 정확하게 증가한다")
    void getPlaceDetail_concurrentAccess() throws InterruptedException {
        // given
        int numberOfThreads = 10; // 10개의 동시 요청 시뮬레이션
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
//        log.info("[동시] 테스트 시작. ({}개 스레드)", numberOfThreads);

        // when
        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    // 각 스레드가 getPlaceDetail 메서드를 호출
//                    log.info("[동시] 스레드 {} -> getPlaceDetail() 호출", Thread.currentThread().getId());
                    placeReadService.getPlaceDetail(testPlace.getId(), testUser.getId());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();
//        log.info("[동시] 10개 스레드 모두 호출 완료");

        // then
        await().atMost(10, TimeUnit.SECONDS) // 10개 스레드이므로 넉넉하게 10초 대기
                .untilAsserted(() -> { // viewCount가 10이 될 때까지 반복
                    Place finalPlace = placeRepository.findById(testPlace.getId()).get();
//                    log.info("[동시] Awaitility 폴링 중... 최종 조회수: {}", finalPlace.getViewCount());
                    assertThat(finalPlace.getViewCount()).isEqualTo(numberOfThreads);
                });
    }

    @Test
    @DisplayName("오늘의 맛집: 오늘 조회수 랭킹이 3곳 이상이면 오늘 랭킹을 반환한다")
    void dailyRanking_returnsToday_whenTodayHasEnough() {
        // given
        LocalDate today = LocalDate.now();
        Place p1 = createApprovedPlace("t1");
        Place p2 = createApprovedPlace("t2");
        Place p3 = createApprovedPlace("t3");
        saveDailyCount(p1, today, 10);
        saveDailyCount(p2, today, 30);
        saveDailyCount(p3, today, 20);

        // when
        List<PlaceCommonResponseDto> result = placeReadService.getRanking(Campus.CHEONAN, SortType.VIEWS);

        // then: 오늘 count DESC 순
        assertThat(result).extracting(PlaceCommonResponseDto::getPlaceId)
                .containsExactly(p2.getId(), p3.getId(), p1.getId());
    }

    @Test
    @DisplayName("오늘의 맛집: 오늘이 3곳 미만이면 어제 랭킹(3곳 이상)으로 폴백한다")
    void dailyRanking_fallsBackToYesterday_whenTodayInsufficient() {
        // given
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        Place p1 = createApprovedPlace("y1");
        Place p2 = createApprovedPlace("y2");
        Place p3 = createApprovedPlace("y3");
        saveDailyCount(p1, today, 5); // 오늘은 1곳뿐 (3 미만)
        saveDailyCount(p1, yesterday, 10);
        saveDailyCount(p2, yesterday, 30);
        saveDailyCount(p3, yesterday, 20);

        // when
        List<PlaceCommonResponseDto> result = placeReadService.getRanking(Campus.CHEONAN, SortType.VIEWS);

        // then: 빈 값이 아니라 어제 랭킹으로 폴백
        assertThat(result).extracting(PlaceCommonResponseDto::getPlaceId)
                .containsExactly(p2.getId(), p3.getId(), p1.getId());
    }

    @Test
    @DisplayName("오늘의 맛집: 오늘·어제 모두 3곳 미만이면 전체 누적 조회수로 폴백한다")
    void dailyRanking_fallsBackToTotalViewCount_whenNoRecentData() {
        // given: 오늘/어제 daily 데이터 없음, 누적 조회수만 존재
        Place p1 = createApprovedPlace("v1");
        Place p2 = createApprovedPlace("v2");
        Place p3 = createApprovedPlace("v3");
        bumpViewCount(p1, 10);
        bumpViewCount(p2, 30);
        bumpViewCount(p3, 20);

        // when
        List<PlaceCommonResponseDto> result = placeReadService.getRanking(Campus.CHEONAN, SortType.VIEWS);

        // then: 빈 값이 아니라 누적 조회수 DESC로 폴백
        assertThat(result).isNotEmpty();
        assertThat(result).extracting(PlaceCommonResponseDto::getPlaceId)
                .containsExactly(p2.getId(), p3.getId(), p1.getId());
    }

    private Place createApprovedPlace(String suffix) {
        return placeRepository.save(Place.builder()
                .name("맛집_" + suffix)
                .address("주소_" + suffix)
                .kakaoPlaceId("kakao_" + suffix)
                .campus(Campus.CHEONAN)
                .latitude(36.1)
                .longitude(127.1)
                .status(PlaceStatus.APPROVED)
                .build());
    }

    private void saveDailyCount(Place place, LocalDate date, int count) {
        dailyViewCountRepository.save(new DailyViewCount(place, date, count));
    }

    private void bumpViewCount(Place place, int times) {
        // incrementViewCount는 @Modifying이라 트랜잭션이 필요하다 (운영에선 서비스 @Transactional 안에서 호출됨)
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            for (int i = 0; i < times; i++) {
                placeRepository.incrementViewCount(place.getId());
            }
        });
    }
}
