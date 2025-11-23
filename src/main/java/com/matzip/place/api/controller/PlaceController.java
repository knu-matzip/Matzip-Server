package com.matzip.place.api.controller;

import com.matzip.common.response.ApiResponse;
import com.matzip.common.security.UserPrincipal;
import com.matzip.place.api.request.PlaceCheckRequestDto;
import com.matzip.place.api.request.PlaceRequestDto;
import com.matzip.place.api.response.PlaceCheckResponseDto;
import com.matzip.place.api.response.PlaceRegisterResponseDto;
import com.matzip.place.api.response.PlaceSearchResponseDto;
import com.matzip.place.application.service.PlaceReadService;
import com.matzip.place.application.service.PlaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/places")
public class PlaceController {

    private final PlaceService placeService;
    private final PlaceReadService placeReadService;

    // 프리뷰
    @GetMapping("/preview")
    public ApiResponse<PlaceCheckResponseDto> preview(@Valid @ModelAttribute PlaceCheckRequestDto req) {
        PlaceCheckResponseDto data = placeService.preview(req);
        return ApiResponse.success(data);
    }

    // 등록
    @PostMapping
    public ApiResponse<PlaceRegisterResponseDto> register(@Valid @RequestBody PlaceRequestDto req) {
        PlaceRegisterResponseDto data = placeService.register(req);
        return ApiResponse.success(data);
    }

    // 검색
    @GetMapping("/search")
    public ApiResponse<List<PlaceSearchResponseDto>> search(@RequestParam String keyword) {
        List<PlaceSearchResponseDto> places = placeReadService.searchPlaceDetails(keyword);
        return ApiResponse.success(places);
    }
}
