package com.matzip.lottery.controller;

import com.matzip.common.response.ApiResponse;
import com.matzip.common.security.UserPrincipal;
import com.matzip.lottery.dto.request.ApplyEventRequestDto;
import com.matzip.lottery.dto.response.ApplyEventResponseDto;
import com.matzip.lottery.dto.response.EventEntryResultResponseDto;
import com.matzip.lottery.dto.response.LotteryEventView;
import com.matzip.lottery.dto.response.ParticipatedEventResponseDto;
import com.matzip.lottery.service.LotteryEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "이벤트", description = "추첨 이벤트 조회 및 응모 API")
@RequestMapping("/api/v1/events")
@RestController
public class LotteryEventController {

    private final LotteryEventService lotteryEventService;

    public LotteryEventController(LotteryEventService lotteryEventService) {
        this.lotteryEventService = lotteryEventService;
    }

    @Operation(summary = "현재 진행 중인 이벤트 조회", description = "진행 중인 추첨 이벤트를 조회한다. 비로그인 시 익명 응답, 로그인 시 사용 티켓 수 등이 포함된다.")
    @GetMapping
    public ApiResponse<LotteryEventView> findEvent(@AuthenticationPrincipal UserPrincipal user) {
        Long userId = (user != null) ? user.getUserId() : null;
        LotteryEventView data = lotteryEventService.getCurrentEvent(userId);
        return ApiResponse.success(data);
    }

    @Operation(summary = "내가 응모한 이벤트 목록", description = "인증된 사용자가 응모한 이벤트 목록을 조회한다. 인증 필요.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", useReturnTypeSchema = true)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증이 필요합니다.")
    @GetMapping("/entries")
    public ApiResponse<List<ParticipatedEventResponseDto>> getParticipatedEvents(@AuthenticationPrincipal UserPrincipal user) {
        List<ParticipatedEventResponseDto> data = lotteryEventService.getParticipatedEvents(user.getUserId());
        return ApiResponse.success(data);
    }

    @Operation(summary = "이벤트 응모 결과 조회", description = "특정 이벤트에 대한 응모/당첨 결과를 조회한다. 인증 필요.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", useReturnTypeSchema = true)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증이 필요합니다.")
    @GetMapping("/{eventId}/entries")
    public ApiResponse<EventEntryResultResponseDto> getEntryResult(@PathVariable Long eventId,
                                                                @AuthenticationPrincipal UserPrincipal user) {
        EventEntryResultResponseDto data = lotteryEventService.getEntryResult(eventId, user.getUserId());
        return ApiResponse.success(data);
    }

    @Operation(summary = "이벤트 응모", description = "연락처와 약관 동의 정보로 이벤트에 응모한다. 인증 필요.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "응모 성공", useReturnTypeSchema = true)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증이 필요합니다.")
    @PostMapping("/{eventId}/apply")
    public ApiResponse<ApplyEventResponseDto> applyForPrize(@PathVariable Long eventId,
                                                         @Validated @RequestBody ApplyEventRequestDto request,
                                                         @AuthenticationPrincipal UserPrincipal user) {
        ApplyEventResponseDto data = lotteryEventService.applyForPrize(eventId, user.getUserId(), request);
        return ApiResponse.success(data);
    }
}
