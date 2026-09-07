package com.matzip.place.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.matzip.AbstractMatzipApplicationTest;
import com.matzip.fixture.UserFixtures;
import com.matzip.place.domain.entity.Place;
import com.matzip.place.repository.PlaceRepository;
import com.matzip.user.domain.User;
import com.matzip.user.repository.UserRepository;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PlaceLikeServiceConcurrencyTest extends AbstractMatzipApplicationTest {

    private static final int USER_COUNT = 20;

    // 풀 크기를 사용자 수 이상으로 두어 실제 USER_COUNT-way 동시성을 보장한다.
    private final ExecutorService executorService = Executors.newFixedThreadPool(USER_COUNT);

    @Autowired
    private PlaceLikeService placeLikeService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PlaceRepository placeRepository;

    @AfterEach
    void tearDown() {
        executorService.shutdownNow();
    }

    @Test
    void 서로_다른_사용자가_동시에_좋아요를_눌러도_좋아요_수가_유실되지_않는다() throws InterruptedException {
        // given: 동일 place에 좋아요를 누를 서로 다른 사용자 N명
        Long placeId = testPlaces.get(0).getId();
        List<User> likers = IntStream.range(0, USER_COUNT)
                .mapToObj(i -> userRepository.save(UserFixtures.createUserWith("liker" + i)))
                .toList();

        // when: 모든 사용자가 동시에 addLike 호출 (startLatch로 동시성 극대화)
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(USER_COUNT);
        ConcurrentLinkedQueue<Exception> failures = new ConcurrentLinkedQueue<>();
        for (User liker : likers) {
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    placeLikeService.addLike(liker.getId(), placeId);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);   // 스택트레이스 (진단용)
                    failures.add(e);                // 테스트 실패 조건 (판정용)
                } finally {
                    doneLatch.countDown();
                }
            });
        }
        startLatch.countDown();
        boolean completed = doneLatch.await(10, TimeUnit.SECONDS);

        // then: 모든 작업이 시간 내 완료되고, 예외 없이, 증가분 유실 없이 정확히 N
        assertThat(completed).isTrue();
        assertThat(failures).isEmpty();
        Place place = placeRepository.findById(placeId).orElseThrow();
        assertThat(place.getLikeCount()).isEqualTo(USER_COUNT);
    }
}
