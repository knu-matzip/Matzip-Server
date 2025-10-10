package com.matzip.place.application;

import com.matzip.place.application.service.PlaceReadService;
import com.matzip.place.domain.Campus;
import com.matzip.place.domain.PlaceStatus;
import com.matzip.place.domain.entity.Place;
import com.matzip.place.infra.repository.DailyViewCountRepository;
import com.matzip.place.infra.repository.PlaceRepository;
import com.matzip.user.domain.User;
import com.matzip.user.infra.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

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
        Place updatedPlace = placeRepository.findById(testPlace.getId()).get();
        assertThat(updatedPlace.getViewCount()).isEqualTo(initialViewCount + 1);
    }

    @Test
    @DisplayName("여러 요청이 동시에 들어와도 조회수가 정확하게 증가한다")
    void getPlaceDetail_concurrentAccess() throws InterruptedException {
        // given
        int numberOfThreads = 10; // 10개의 동시 요청 시뮬레이션
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        // when
        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    // 각 스레드가 getPlaceDetail 메서드를 호출
                    placeReadService.getPlaceDetail(testPlace.getId(), testUser.getId());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        Place finalPlace = placeRepository.findById(testPlace.getId()).get();
        assertThat(finalPlace.getViewCount()).isEqualTo(numberOfThreads);
    }
}
