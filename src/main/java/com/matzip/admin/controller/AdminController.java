package com.matzip.admin.controller;

import com.matzip.admin.controller.dto.PlaceRegisterRequestDetailResponse;
import com.matzip.admin.controller.dto.PlaceRegisterRequestsResponse;
import com.matzip.common.exception.BusinessException;
import com.matzip.common.exception.code.ErrorCode;
import com.matzip.common.response.ApiResponse;
import com.matzip.place.infra.repository.PlaceRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/admin/api")
@RestController
public class AdminController {

    private final PlaceRepository placeRepository;

    public AdminController(PlaceRepository placeRepository) {
        this.placeRepository = placeRepository;
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
}
