package com.matzip.admin.controller;

import com.matzip.admin.controller.request.PlaceRegisterRequestReviewRequestDto;
import com.matzip.admin.controller.response.PlaceRegisterRequestDetailResponseDto;
import com.matzip.admin.controller.response.PlaceRegisterRequestsResponseDto;
import com.matzip.admin.service.AdminPlaceRegisterRequestService;
import com.matzip.common.exception.BusinessException;
import com.matzip.common.exception.code.ErrorCode;
import com.matzip.common.response.ApiResponse;
// import com.matzip.common.security.UserPrincipal;
import com.matzip.place.repository.PlaceRepository;
// import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/admin/api")
@RestController
public class AdminController {

    private final PlaceRepository placeRepository;
    private final AdminPlaceRegisterRequestService adminPlaceRegisterRequestService;

    public AdminController(PlaceRepository placeRepository, AdminPlaceRegisterRequestService adminPlaceRegisterRequestService) {
        this.placeRepository = placeRepository;
        this.adminPlaceRegisterRequestService = adminPlaceRegisterRequestService;
    }

    @GetMapping("/requests/places")
    public ApiResponse<List<PlaceRegisterRequestsResponseDto>> findPlaceRegisterRequests() {
        List<PlaceRegisterRequestsResponseDto> data = placeRepository.findPendingPlaces()
                .stream()
                .map(PlaceRegisterRequestsResponseDto::from)
                .toList();

        return ApiResponse.success(data);
    }

    @GetMapping("/requests/places/{placeId}")
    public ApiResponse<PlaceRegisterRequestDetailResponseDto> findPlaceRegisterRequestDetail(
            @PathVariable("placeId") Long placeId
    ) {
        PlaceRegisterRequestDetailResponseDto data = placeRepository.findByIdWithCategoriesAndTags(placeId)
                .map(PlaceRegisterRequestDetailResponseDto::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_FOUND));

        return ApiResponse.success(data);
    }

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
