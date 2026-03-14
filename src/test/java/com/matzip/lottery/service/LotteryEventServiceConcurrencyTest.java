package com.matzip.lottery.service;

import com.matzip.AbstractMatzipApplicationTest;
import com.matzip.lottery.domain.LotteryEntry;
import com.matzip.lottery.domain.LotteryEvent;
import com.matzip.lottery.domain.Prize;
import com.matzip.lottery.domain.Ticket;
import com.matzip.lottery.repository.LotteryEntryRepository;
import com.matzip.lottery.repository.LotteryEventRepository;
import com.matzip.lottery.repository.TicketRepository;
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
import java.util.stream.IntStream;

class LotteryEventServiceConcurrencyTest extends AbstractMatzipApplicationTest {

    private final ExecutorService executorService = Executors.newFixedThreadPool(3);

    @Autowired
    private LotteryEventService lotteryEventService;
    @Autowired
    private LotteryEventRepository lotteryEventRepository;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private LotteryEntryRepository lotteryEntryRepository;

    private LotteryEvent testLotteryEvent;

    @BeforeEach
    void setUp() {
        List<Ticket> tickets = IntStream.range(0, 2)
                .mapToObj(index -> {
                    Ticket ticket = Ticket.builder()
                            .status(Ticket.Status.ACTIVE)
                            .userId(super.testUser.getId())
                            .placeId(this.testPlaces.get(index).getId())
                            .build();
                    return ticketRepository.save(ticket);
                })
                .toList();
        log.info("tickets: {}", tickets);

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
        int ticketsCount = 2;

        CountDownLatch countDownLatch = new CountDownLatch(parallel);
        for (int i = 0; i < parallel; i++) {
            executorService.submit(() -> {
                try {
                    lotteryEventService.enterLottery(testLotteryEvent.getId(), ticketsCount, testUser.getId());
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        countDownLatch.await();

        List<LotteryEntry> entries = lotteryEntryRepository.findByLotteryEvent(testLotteryEvent);
        Assertions.assertThat(entries).hasSize(ticketsCount);
    }
}