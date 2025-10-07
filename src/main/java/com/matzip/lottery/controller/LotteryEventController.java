package com.matzip.lottery.controller;

import com.matzip.common.response.ApiResponse;
import com.matzip.lottery.controller.response.LotteryEventResponse;
import com.matzip.lottery.service.LotteryEventService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
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
    public ApiResponse<LotteryEventResponse> findEvent(@AuthenticationPrincipal UserDetails user) {
        LotteryEventResponse data = lotteryEventService.getCurrentEvent(Long.parseLong(user.getUsername()));
        return ApiResponse.success(data);
    }
}
