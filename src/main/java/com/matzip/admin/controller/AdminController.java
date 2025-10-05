package com.matzip.admin.controller;

import com.matzip.admin.controller.dto.PlaceRegisterRequestsResponse;
import com.matzip.common.response.ApiResponse;
import com.matzip.place.infra.repository.PlaceRepository;
import org.springframework.web.bind.annotation.GetMapping;
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
}
