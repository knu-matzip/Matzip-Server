package com.matzip.place.application;

import com.matzip.place.application.service.PlaceReadService;
import com.matzip.place.domain.Campus;
import com.matzip.place.domain.PlaceStatus;
import com.matzip.place.domain.entity.Place;
import com.matzip.place.infra.repository.DailyViewCountRepository;
import com.matzip.place.infra.repository.PlaceRepository;
import com.matzip.user.domain.User;
import com.matzip.user.infra.UserRepository;
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
        log.info("[동시] 10개 스레드 모두 호출 완료");

        // then
        await().atMost(10, TimeUnit.SECONDS) // 10개 스레드이므로 넉넉하게 10초 대기
                .untilAsserted(() -> { // viewCount가 10이 될 때까지 반복
                    Place finalPlace = placeRepository.findById(testPlace.getId()).get();
//                    log.info("[동시] Awaitility 폴링 중... 최종 조회수: {}", finalPlace.getViewCount());
                    assertThat(finalPlace.getViewCount()).isEqualTo(numberOfThreads);
                });
    }
}
