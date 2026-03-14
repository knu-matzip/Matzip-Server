package com.matzip.lottery.service;

import com.matzip.lottery.domain.Ticket;
import com.matzip.lottery.repository.TicketRepository;
import com.matzip.place.domain.PlaceStatus;
import com.matzip.place.domain.entity.Place;
import com.matzip.user.domain.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class TicketIssuanceServiceTest {

    private static final Logger log = LoggerFactory.getLogger(TicketIssuanceServiceTest.class);

    @InjectMocks
    private TicketIssuanceService ticketIssuanceService;

    @Mock
    private TicketRepository ticketRepository;

    @Test
    @DisplayName("응모권이 정상적으로 발급된다.")
    void issueTicket_Success() {
        //given
        User user = User.builder()
                .id(1L)
                .build();
        Place place = Place.builder()
                .id(1L)
                .status(PlaceStatus.APPROVED)
                .build();

        //when
        Ticket ticket = ticketIssuanceService.issueTicket(user, place);

        //then
        Assertions.assertThat(ticket).isNotNull();
        Assertions.assertThat(ticket.getStatus()).isEqualTo(Ticket.Status.ACTIVE);
        Assertions.assertThat(ticket.getPlaceId()).isEqualTo(place.getId());
        Assertions.assertThat(ticket.getPlaceId()).isEqualTo(place.getId());
    }

    @Test
    @DisplayName("이미 응모권이 발급된 경우 예외가 발생한다.")
    void issueTicket_AlreadyIssued() {
        //given
        User user = User.builder()
                .id(1L)
                .build();
        Place place = Place.builder()
                .id(1L)
                .status(PlaceStatus.APPROVED)
                .build();

        //when & then
        BDDMockito.given(ticketRepository.findByUserIdAndPlaceId(user.getId(), place.getId()))
                .willReturn(Optional.of(new Ticket()));

        Assertions.assertThatThrownBy(() -> ticketIssuanceService.issueTicket(user, place))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 응모권 발급이 완료되었습니다.");
    }

    @Test
    @DisplayName("등록이 완료되지 않은 장소에 대해서는 응모권이 발급되지 않는다.")
    void issueTicket_PlaceNotAccepted() {
        // TODO: Place에 status 추가 및 검증 로직 구현 후 테스트 작성
    }
}