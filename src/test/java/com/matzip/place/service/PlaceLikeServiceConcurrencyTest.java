package com.matzip.place.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.matzip.AbstractMatzipApplicationTest;
import com.matzip.fixture.UserFixtures;
import com.matzip.place.domain.entity.Place;
import com.matzip.place.repository.PlaceRepository;
import com.matzip.user.domain.User;
import com.matzip.user.repository.UserRepository;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PlaceLikeServiceConcurrencyTest extends AbstractMatzipApplicationTest {

    private final ExecutorService executorService = Executors.newFixedThreadPool(16);

    @Autowired
    private PlaceLikeService placeLikeService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PlaceRepository placeRepository;

    @AfterEach
    void tearDown() {
        executorService.shutdown();
    }

    @Test
    void 서로_다른_사용자가_동시에_좋아요를_눌러도_좋아요_수가_유실되지_않는다() throws InterruptedException {
        // given: 동일 place에 좋아요를 누를 서로 다른 사용자 N명
        int userCount = 20;
        Long placeId = testPlaces.get(0).getId();
        List<User> likers = IntStream.range(0, userCount)
                .mapToObj(i -> userRepository.save(UserFixtures.createUserWith("liker" + i)))
                .toList();

        // when: 모든 사용자가 동시에 addLike 호출
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(userCount);
        for (User liker : likers) {
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    placeLikeService.addLike(liker.getId(), placeId);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }
        startLatch.countDown();
        doneLatch.await();

        // then: 원자적 UPDATE라면 증가분 유실 없이 정확히 N
        Place place = placeRepository.findById(placeId).orElseThrow();
        assertThat(place.getLikeCount()).isEqualTo(userCount);
    }
}
