package com.matzip.place.api.controller;

import com.matzip.admin.controller.response.PlaceRegisterRequestDetailResponse;
import com.matzip.common.exception.BusinessException;
import com.matzip.common.exception.code.ErrorCode;
import com.matzip.common.response.ApiResponse;
import com.matzip.common.security.UserPrincipal;
import com.matzip.place.api.response.PlaceRegisterStatusDetailResponseDto;
import com.matzip.place.api.response.PlaceRegisterStatusResponseDto;
import com.matzip.place.application.service.PlaceReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/requests/places")
public class PlaceRequestController {

    private final PlaceReadService placeReadService;

    @GetMapping
    public ApiResponse<List<PlaceRegisterStatusResponseDto>> getMyPlaceRequests(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        if (userPrincipal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        List<PlaceRegisterStatusResponseDto> response = placeReadService.getMyPlaceRequests(userPrincipal.getUserId());
        return ApiResponse.success(response);
    }

    @GetMapping("/{placeId}")
    public ApiResponse<PlaceRegisterStatusDetailResponseDto> getMyPlaceRequestDetail(
            @PathVariable Long placeId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        if (userPrincipal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        PlaceRegisterStatusDetailResponseDto response = placeReadService.getMyPlaceRequestDetail(placeId, userPrincipal.getUserId());
        return ApiResponse.success(response);
    }


}
