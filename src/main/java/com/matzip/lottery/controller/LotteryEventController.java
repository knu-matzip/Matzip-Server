package com.matzip.lottery.controller;

import com.matzip.common.response.ApiResponse;
import com.matzip.common.security.UserPrincipal;
import com.matzip.lottery.controller.request.ApplyEventRequestDto;
import com.matzip.lottery.controller.response.ApplyEventResponseDto;
import com.matzip.lottery.controller.response.EventEntryResultResponseDto;
import com.matzip.lottery.controller.response.LotteryEventView;
import com.matzip.lottery.controller.response.ParticipatedEventResponseDto;
import com.matzip.lottery.service.LotteryEventService;

import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/events")
@RestController
public class LotteryEventController {

    private final LotteryEventService lotteryEventService;

    public LotteryEventController(LotteryEventService lotteryEventService) {
        this.lotteryEventService = lotteryEventService;
    }

    @GetMapping
    public ApiResponse<LotteryEventView> findEvent(@AuthenticationPrincipal UserPrincipal user) {
        Long userId = (user != null) ? user.getUserId() : null;
        LotteryEventView data = lotteryEventService.getCurrentEvent(userId);
        return ApiResponse.success(data);
    }

    @GetMapping("/entries")
    public ApiResponse<List<ParticipatedEventResponseDto>> getParticipatedEvents(@AuthenticationPrincipal UserPrincipal user) {
        List<ParticipatedEventResponseDto> data = lotteryEventService.getParticipatedEvents(user.getUserId());
        return ApiResponse.success(data);
    }

    @GetMapping("/{eventId}/entries")
    public ApiResponse<EventEntryResultResponseDto> getEntryResult(@PathVariable Long eventId,
                                                                @AuthenticationPrincipal UserPrincipal user) {
        EventEntryResultResponseDto data = lotteryEventService.getEntryResult(eventId, user.getUserId());
        return ApiResponse.success(data);
    }

    @PostMapping("/{eventId}/apply")
    public ApiResponse<ApplyEventResponseDto> applyForPrize(@PathVariable Long eventId,
                                                         @Validated @RequestBody ApplyEventRequestDto request,
                                                         @AuthenticationPrincipal UserPrincipal user) {
        ApplyEventResponseDto data = lotteryEventService.applyForPrize(eventId, user.getUserId(), request);
        return ApiResponse.success(data);
    }
}
