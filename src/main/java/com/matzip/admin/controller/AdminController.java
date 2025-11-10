package com.matzip.admin.controller;

import com.matzip.admin.controller.request.PlaceRegisterRequestReviewRequest;
import com.matzip.admin.controller.response.PlaceRegisterRequestDetailResponse;
import com.matzip.admin.controller.response.PlaceRegisterRequestsResponse;
import com.matzip.admin.service.AdminPlaceRegisterRequestService;
import com.matzip.common.exception.BusinessException;
import com.matzip.common.exception.code.ErrorCode;
import com.matzip.common.response.ApiResponse;
import com.matzip.common.security.UserPrincipal;
import com.matzip.place.infra.repository.PlaceRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ApiResponse<List<PlaceRegisterRequestsResponse>> findPlaceRegisterRequests() {
        List<PlaceRegisterRequestsResponse> data = placeRepository.findPendingPlaces()
                .stream()
                .map(PlaceRegisterRequestsResponse::from)
                .toList();

        return ApiResponse.success(data);
    }

    @GetMapping("/requests/places/{placeId}")
    public ApiResponse<PlaceRegisterRequestDetailResponse> findPlaceRegisterRequestDetail(
            @PathVariable("placeId") Long placeId
    ) {
        PlaceRegisterRequestDetailResponse data = placeRepository.findByIdWithCategoriesAndTags(placeId)
                .map(PlaceRegisterRequestDetailResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_FOUND));

        return ApiResponse.success(data);
    }

    @PostMapping("/requests/places/{placeId}/review")
    public ApiResponse<?> reviewPlaceRegisterRequest(
            @PathVariable("placeId") Long placeId,
            @RequestBody PlaceRegisterRequestReviewRequest request
//            @AuthenticationPrincipal UserPrincipal admin
    ) {
        adminPlaceRegisterRequestService.review(
                placeId, request.status(), request.rejectedReason()
        );
        return ApiResponse.successWithoutData();
    }
}
