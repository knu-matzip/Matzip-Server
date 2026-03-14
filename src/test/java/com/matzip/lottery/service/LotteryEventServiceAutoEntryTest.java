package com.matzip.lottery.service;

import com.matzip.lottery.domain.LotteryEvent;
import com.matzip.lottery.domain.Prize;
import com.matzip.lottery.repository.LotteryEntryRepository;
import com.matzip.lottery.repository.LotteryEventRepository;
import com.matzip.lottery.repository.WinnerContactRepository;
import com.matzip.lottery.repository.WinnerRepository;
import com.matzip.lottery.infra.discord.DiscordWinnerNotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LotteryEventServiceAutoEntryTest {

    @InjectMocks
    private LotteryEventService lotteryEventService;

    @Mock
    private LotteryEventRepository lotteryEventRepository;
    @Mock
    private LotteryEntryRepository lotteryEntryRepository;
    @Mock
    private WinnerRepository winnerRepository;
    @Mock
    private WinnerContactRepository winnerContactRepository;
    @Mock
    private DiscordWinnerNotificationService discordWinnerNotificationService;

    @Test
    @DisplayName("진행 중인 이벤트가 있으면 승인된 장소를 현재 이벤트에 자동 응모한다")
    void enterCurrentEventOnPlaceApprovalSuccess() {
        LotteryEvent currentEvent = createCurrentEvent(1L);
        BDDMockito.given(lotteryEventRepository.findCurrentEvent(any(LocalDateTime.class)))
                .willReturn(Optional.of(currentEvent));
        BDDMockito.given(lotteryEntryRepository.existsByLotteryEvent_IdAndPlaceId(currentEvent.getId(), 10L))
                .willReturn(false);

        boolean entered = lotteryEventService.enterCurrentEventOnPlaceApproval(100L, 10L);

        assertThat(entered).isTrue();
        verify(lotteryEntryRepository).save(any());
    }

    @Test
    @DisplayName("진행 중 이벤트가 없으면 자동 응모하지 않는다")
    void skipWhenNoCurrentEvent() {
        BDDMockito.given(lotteryEventRepository.findCurrentEvent(any(LocalDateTime.class)))
                .willReturn(Optional.empty());

        boolean entered = lotteryEventService.enterCurrentEventOnPlaceApproval(100L, 10L);

        assertThat(entered).isFalse();
        verify(lotteryEntryRepository, never()).save(any());
    }

    @Test
    @DisplayName("이미 같은 장소가 현재 이벤트에 자동 응모된 경우 중복 생성하지 않는다")
    void skipWhenAlreadyEntered() {
        LotteryEvent currentEvent = createCurrentEvent(1L);
        BDDMockito.given(lotteryEventRepository.findCurrentEvent(any(LocalDateTime.class)))
                .willReturn(Optional.of(currentEvent));
        BDDMockito.given(lotteryEntryRepository.existsByLotteryEvent_IdAndPlaceId(anyLong(), anyLong()))
                .willReturn(true);

        boolean entered = lotteryEventService.enterCurrentEventOnPlaceApproval(100L, 10L);

        assertThat(entered).isFalse();
        verify(lotteryEntryRepository, never()).save(any());
    }

    private LotteryEvent createCurrentEvent(Long eventId) {
        LotteryEvent event = LotteryEvent.builder()
                .prize(Prize.builder()
                        .description("상품")
                        .imageUrl("https://example.com/image.png")
                        .build())
                .winnersCount(1)
                .endDateTime(LocalDateTime.now().plusDays(1))
                .build();
        ReflectionTestUtils.setField(event, "id", eventId);
        return event;
    }
}
