package com.matzip.place.controller;

import com.matzip.admin.dto.response.PlaceRegisterRequestDetailResponseDto;
import com.matzip.common.exception.BusinessException;
import com.matzip.common.exception.code.ErrorCode;
import com.matzip.common.response.ApiResponse;
import com.matzip.common.security.UserPrincipal;
import com.matzip.place.dto.response.PlaceRegisterStatusDetailResponseDto;
import com.matzip.place.dto.response.PlaceRegisterStatusResponseDto;
import com.matzip.place.service.PlaceReadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "맛집 등록 요청", description = "내 맛집 등록 요청 현황 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/requests/places")
public class PlaceRequestController {

    private final PlaceReadService placeReadService;

    @Operation(summary = "내 맛집 등록 요청 목록", description = "인증된 사용자가 등록 요청한 맛집들의 승인 상태 목록을 조회한다. 인증 필요.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요합니다.")
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

    @Operation(summary = "내 맛집 등록 요청 상세", description = "인증된 사용자의 특정 맛집 등록 요청 상세와 승인/거절 사유를 조회한다. 인증 필요.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요합니다.")
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
