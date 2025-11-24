package com.matzip.user.api.controller;

import com.matzip.common.response.ApiResponse;
import com.matzip.common.security.UserPrincipal;
import com.matzip.user.api.response.UserProfileResponseDto;
import com.matzip.user.application.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserProfileResponseDto> getMyProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        UserProfileResponseDto profile = userService.getUserProfile(userPrincipal.getUserId());
        return ApiResponse.success(profile);
    }
}
