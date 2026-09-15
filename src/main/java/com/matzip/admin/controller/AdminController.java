package com.matzip.admin.controller;

import com.matzip.admin.dto.request.PlaceRegisterRequestReviewRequestDto;
import com.matzip.admin.dto.response.PlaceRegisterRequestDetailResponseDto;
import com.matzip.admin.dto.response.PlaceRegisterRequestsResponseDto;
import com.matzip.admin.service.AdminPlaceRegisterRequestService;
import com.matzip.common.exception.BusinessException;
import com.matzip.common.exception.code.ErrorCode;
import com.matzip.common.response.ApiResponse;
// import com.matzip.common.security.UserPrincipal;
import com.matzip.place.repository.PlaceRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
// import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "어드민", description = "맛집 등록 요청 승인/거절 관리 API")
@RequestMapping("/admin/api")
@RestController
public class AdminController {

    private final PlaceRepository placeRepository;
    private final AdminPlaceRegisterRequestService adminPlaceRegisterRequestService;

    public AdminController(PlaceRepository placeRepository, AdminPlaceRegisterRequestService adminPlaceRegisterRequestService) {
        this.placeRepository = placeRepository;
        this.adminPlaceRegisterRequestService = adminPlaceRegisterRequestService;
    }

    @Operation(summary = "맛집 등록 요청 목록 (어드민)", description = "승인 대기(PENDING) 상태인 맛집 등록 요청 목록을 조회한다.")
    @GetMapping("/requests/places")
    public ApiResponse<List<PlaceRegisterRequestsResponseDto>> findPlaceRegisterRequests() {
        List<PlaceRegisterRequestsResponseDto> data = placeRepository.findPendingPlaces()
                .stream()
                .map(PlaceRegisterRequestsResponseDto::from)
                .toList();

        return ApiResponse.success(data);
    }

    @Operation(summary = "맛집 등록 요청 상세 (어드민)", description = "특정 맛집 등록 요청의 상세 정보를 조회한다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", useReturnTypeSchema = true)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "맛집을 찾을 수 없습니다.")
    @GetMapping("/requests/places/{placeId}")
    public ApiResponse<PlaceRegisterRequestDetailResponseDto> findPlaceRegisterRequestDetail(
            @PathVariable("placeId") Long placeId
    ) {
        PlaceRegisterRequestDetailResponseDto data = placeRepository.findByIdWithCategoriesAndTags(placeId)
                .map(PlaceRegisterRequestDetailResponseDto::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_FOUND));

        return ApiResponse.success(data);
    }

    @Operation(summary = "맛집 등록 요청 승인/거절 (어드민)", description = "맛집 등록 요청을 승인(APPROVED) 또는 거절(REJECTED)한다. 거절 시 사유를 함께 전달한다.")
    @PostMapping("/requests/places/{placeId}/review")
    public ApiResponse<?> reviewPlaceRegisterRequest(
            @PathVariable("placeId") Long placeId,
            @RequestBody PlaceRegisterRequestReviewRequestDto request
            // , @AuthenticationPrincipal UserPrincipal admin
    ) {
        adminPlaceRegisterRequestService.review(
                placeId, request.status(), request.rejectedReason()
                // , admin.getUserId()
        );
        return ApiResponse.successWithoutData();
    }
}
