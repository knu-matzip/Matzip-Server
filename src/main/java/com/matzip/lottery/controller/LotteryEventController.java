package com.matzip.lottery.controller;

import com.matzip.common.response.ApiResponse;
import com.matzip.common.security.UserPrincipal;
import com.matzip.lottery.controller.request.ParticipateEventRequest;
import com.matzip.lottery.controller.response.LotteryEventResponse;
import com.matzip.lottery.service.LotteryEventService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
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
    public ApiResponse<LotteryEventResponse> findEvent(@AuthenticationPrincipal UserPrincipal user) {
        LotteryEventResponse data = lotteryEventService.getCurrentEvent(user.getUserId());
        return ApiResponse.success(data);
    }

    @PostMapping("/entries")
    public ApiResponse<String> participate(@Validated @RequestBody ParticipateEventRequest request,
                                           @AuthenticationPrincipal UserPrincipal user) {
        lotteryEventService.enterLottery(request.eventId(), request.ticketsCount(), user.getUserId());
        return ApiResponse.successWithoutData();
    }
}
