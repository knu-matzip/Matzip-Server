package com.matzip.user.controller;

import com.matzip.common.response.ApiResponse;
import com.matzip.common.security.UserPrincipal;
import com.matzip.user.dto.response.UserProfileResponseDto;
import com.matzip.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "사용자", description = "사용자 프로필 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 프로필 조회", description = "인증된 사용자의 프로필(닉네임/프로필 이미지/배경)을 조회한다. 인증 필요.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", useReturnTypeSchema = true)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증이 필요합니다.")
    @GetMapping("/me")
    public ApiResponse<UserProfileResponseDto> getMyProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        UserProfileResponseDto profile = userService.getUserProfile(userPrincipal.getUserId());
        return ApiResponse.success(profile);
    }
}
