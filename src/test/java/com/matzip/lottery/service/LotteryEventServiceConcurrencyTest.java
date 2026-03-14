package com.matzip.lottery.service;

import com.matzip.AbstractMatzipApplicationTest;
import com.matzip.lottery.domain.LotteryEntry;
import com.matzip.lottery.domain.LotteryEvent;
import com.matzip.lottery.domain.Prize;
import com.matzip.lottery.repository.LotteryEntryRepository;
import com.matzip.lottery.repository.LotteryEventRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class LotteryEventServiceConcurrencyTest extends AbstractMatzipApplicationTest {

    private final ExecutorService executorService = Executors.newFixedThreadPool(3);

    @Autowired
    private LotteryEventService lotteryEventService;
    @Autowired
    private LotteryEventRepository lotteryEventRepository;
    @Autowired
    private LotteryEntryRepository lotteryEntryRepository;

    private LotteryEvent testLotteryEvent;

    @BeforeEach
    void setUpLotteryEvent() {
        LotteryEvent lotteryEvent = LotteryEvent.builder()
                .prize(Prize.builder()
                        .description("상품")
                        .imageUrl("xxx")
                        .build())
                .winnersCount(3)
                .endDateTime(LocalDateTime.now().plusMinutes(10))
                .build();
        this.testLotteryEvent = lotteryEventRepository.save(lotteryEvent);
    }

    @AfterEach
    void tearDown() {
        executorService.shutdown();
    }

    @Test
    void concurrencyTest() throws InterruptedException {
        int parallel = 2;
        Long placeId = testPlaces.get(0).getId();

        CountDownLatch countDownLatch = new CountDownLatch(parallel);
        for (int i = 0; i < parallel; i++) {
            executorService.submit(() -> {
                try {
                    lotteryEventService.enterCurrentEventOnPlaceApproval(testUser.getId(), placeId);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        countDownLatch.await();

        List<LotteryEntry> entries = lotteryEntryRepository.findByLotteryEvent(testLotteryEvent);
        Assertions.assertThat(entries).hasSize(1);
        Assertions.assertThat(entries.get(0).getUserId()).isEqualTo(testUser.getId());
        Assertions.assertThat(entries.get(0).getPlaceId()).isEqualTo(placeId);
    }
}