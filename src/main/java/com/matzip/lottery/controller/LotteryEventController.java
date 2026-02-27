package com.matzip.lottery.controller;

import com.matzip.common.response.ApiResponse;
import com.matzip.common.security.UserPrincipal;
import com.matzip.lottery.controller.request.ParticipateEventRequest;
import com.matzip.lottery.controller.response.EventEntryResultResponse;
import com.matzip.lottery.controller.response.LotteryEventView;
import com.matzip.lottery.controller.response.ParticipatedEventResponse;
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
    public ApiResponse<List<ParticipatedEventResponse>> getParticipatedEvents(@AuthenticationPrincipal UserPrincipal user) {
        List<ParticipatedEventResponse> data = lotteryEventService.getParticipatedEvents(user.getUserId());
        return ApiResponse.success(data);
    }

    @GetMapping("/{eventId}/entries")
    public ApiResponse<EventEntryResultResponse> getEntryResult(@PathVariable Long eventId,
                                                                @AuthenticationPrincipal UserPrincipal user) {
        EventEntryResultResponse data = lotteryEventService.getEntryResult(eventId, user.getUserId());
        return ApiResponse.success(data);
    }

    @PostMapping("/entries")
    public ApiResponse<String> participate(@Validated @RequestBody ParticipateEventRequest request,
                                           @AuthenticationPrincipal UserPrincipal user) {
        lotteryEventService.enterLottery(request.eventId(), request.ticketsCount(), user.getUserId());
        return ApiResponse.successWithoutData();
    }
}
