package com.matzip.lottery.service;

import com.matzip.common.exception.BusinessException;
import com.matzip.common.exception.code.ErrorCode;
import com.matzip.lottery.controller.response.EventEntryResultResponse;
import com.matzip.lottery.domain.LotteryEvent;
import com.matzip.lottery.domain.Prize;
import com.matzip.lottery.domain.Winner;
import com.matzip.lottery.repository.LotteryEntryRepository;
import com.matzip.lottery.repository.LotteryEventRepository;
import com.matzip.lottery.repository.TicketRepository;
import com.matzip.lottery.repository.WinnerContactRepository;
import com.matzip.lottery.repository.WinnerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class LotteryEventServiceGetEntryResultTest {

    private static final Long EVENT_ID = 1L;
    private static final Long USER_ID = 100L;

    @InjectMocks
    private LotteryEventService lotteryEventService;

    @Mock
    private LotteryEventRepository lotteryEventRepository;
    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private LotteryEntryRepository lotteryEntryRepository;
    @Mock
    private WinnerRepository winnerRepository;
    @Mock
    private WinnerContactRepository winnerContactRepository;

    private static LotteryEvent createEvent(Long eventId) {
        LotteryEvent event = LotteryEvent.builder()
                .prize(Prize.builder()
                        .description("BHC 뿌링클 치킨 기프티콘")
                        .imageUrl("https://example.com/images/bhc.png")
                        .build())
                .winnersCount(3)
                .isDrawn(true)
                .endDateTime(LocalDateTime.of(2025, 8, 21, 0, 0))
                .build();
        if (eventId != null) {
            ReflectionTestUtils.setField(event, "id", eventId);
        }
        return event;
    }

    @Nested
    @DisplayName("getEntryResult 성공")
    class GetEntryResultSuccess {

        @Test
        @DisplayName("참여자이고 당첨자인 경우 isWinner=true, 응모 결과를 반환한다")
        void 참여자_당첨_성공() {
            // given
            LotteryEvent event = createEvent(EVENT_ID);
            BDDMockito.given(lotteryEventRepository.findById(EVENT_ID)).willReturn(Optional.of(event));
            BDDMockito.given(lotteryEntryRepository.countByLotteryEventIdAndUserId(EVENT_ID, USER_ID)).willReturn(3);
            BDDMockito.given(lotteryEntryRepository.countParticipantsByLotteryEvent(event)).willReturn(127);
            BDDMockito.given(winnerRepository.findByUserIdAndEventId(USER_ID, EVENT_ID))
                    .willReturn(Optional.of(Winner.builder().userId(USER_ID).eventId(EVENT_ID).build()));
            BDDMockito.given(winnerContactRepository.findByUserIdAndEventId(anyLong(), anyLong())).willReturn(Optional.empty());

            // when
            EventEntryResultResponse result = lotteryEventService.getEntryResult(EVENT_ID, USER_ID);

            // then
            assertThat(result.eventId()).isEqualTo(EVENT_ID);
            assertThat(result.prize()).isNotNull();
            assertThat(result.totalWinnersCount()).isEqualTo(3);
            assertThat(result.participantsCount()).isEqualTo(127);
            assertThat(result.usedTicketsCount()).isEqualTo(3);
            assertThat(result.isWinner()).isTrue();
            assertThat(result.eventEndDate()).isEqualTo(LocalDateTime.of(2025, 8, 21, 0, 0));
            assertThat(result.isPhoneSubmitted()).isFalse();
        }

        @Test
        @DisplayName("참여자이지만 미당첨인 경우 isWinner=false, 응모 결과를 반환한다")
        void 참여자_미당첨_성공() {
            // given
            LotteryEvent event = createEvent(EVENT_ID);
            BDDMockito.given(lotteryEventRepository.findById(EVENT_ID)).willReturn(Optional.of(event));
            BDDMockito.given(lotteryEntryRepository.countByLotteryEventIdAndUserId(EVENT_ID, USER_ID)).willReturn(1);
            BDDMockito.given(lotteryEntryRepository.countParticipantsByLotteryEvent(event)).willReturn(50);
            BDDMockito.given(winnerRepository.findByUserIdAndEventId(USER_ID, EVENT_ID)).willReturn(Optional.empty());
            BDDMockito.given(winnerContactRepository.findByUserIdAndEventId(anyLong(), anyLong())).willReturn(Optional.empty());

            // when
            EventEntryResultResponse result = lotteryEventService.getEntryResult(EVENT_ID, USER_ID);

            // then
            assertThat(result.eventId()).isEqualTo(EVENT_ID);
            assertThat(result.participantsCount()).isEqualTo(50);
            assertThat(result.usedTicketsCount()).isEqualTo(1);
            assertThat(result.isWinner()).isFalse();
            assertThat(result.isPhoneSubmitted()).isFalse();
        }
    }

    @Nested
    @DisplayName("getEntryResult 실패")
    class GetEntryResultFailure {

        @Test
        @DisplayName("존재하지 않는 이벤트 조회 시 BusinessException(INVALID_INPUT_VALUE)을 던진다")
        void 이벤트_없음_예외() {
            // given
            BDDMockito.given(lotteryEventRepository.findById(EVENT_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> lotteryEventService.getEntryResult(EVENT_ID, USER_ID))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException be = (BusinessException) ex;
                        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
                        assertThat(be.getMessage()).contains("이벤트가 존재하지 않습니다");
                    });
        }

        @Test
        @DisplayName("참여하지 않은 이벤트 조회 시 BusinessException(EVENT_NOT_PARTICIPATED)을 던진다")
        void 비참여자_예외() {
            // given
            LotteryEvent event = createEvent(EVENT_ID);
            BDDMockito.given(lotteryEventRepository.findById(EVENT_ID)).willReturn(Optional.of(event));
            BDDMockito.given(lotteryEntryRepository.countByLotteryEventIdAndUserId(EVENT_ID, USER_ID)).willReturn(0);

            // when & then
            assertThatThrownBy(() -> lotteryEventService.getEntryResult(EVENT_ID, USER_ID))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException be = (BusinessException) ex;
                        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.EVENT_NOT_PARTICIPATED);
                        assertThat(be.getMessage()).isEqualTo(ErrorCode.EVENT_NOT_PARTICIPATED.getMessage());
                    });
        }
    }
}
