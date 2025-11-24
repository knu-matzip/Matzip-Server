package com.matzip.place.api.controller;

import com.matzip.common.response.ApiResponse;
import com.matzip.common.security.UserPrincipal;
import com.matzip.place.api.request.MapSearchRequestDto;
import com.matzip.place.api.response.PlaceCommonResponseDto;
import jakarta.validation.Valid;
import com.matzip.place.api.response.MapSearchResponseDto;
import com.matzip.place.api.response.PlaceDetailResponseDto;
import com.matzip.place.application.service.PlaceReadService;
import com.matzip.place.domain.Campus;
import com.matzip.place.domain.SortType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/places")
@RequiredArgsConstructor
public class PlaceReadController {

    private final PlaceReadService placeReadService;

    @GetMapping("/{placeId}")
    public ApiResponse<PlaceDetailResponseDto> getPlaceDetail(
            @PathVariable Long placeId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        Long userId = userPrincipal != null ? userPrincipal.getUserId() : null;
        PlaceDetailResponseDto placeDetail = placeReadService.getPlaceDetail(placeId, userId);
        
        return ApiResponse.success(placeDetail);
    }

    @GetMapping
    public ApiResponse<List<MapSearchResponseDto>> getPlacesInMap(
            @Valid @ModelAttribute MapSearchRequestDto requestDto) {

        List<MapSearchResponseDto> places = placeReadService.findPlacesInMapBounds(requestDto);
        return ApiResponse.success(places);
    }

    @GetMapping("/ranking")
    public ApiResponse<List<PlaceCommonResponseDto>> getRanking(
            @RequestParam SortType sort,
            @RequestParam Campus campus) {

        List<PlaceCommonResponseDto> ranking = placeReadService.getRanking(campus, sort);
        return ApiResponse.success(ranking);
    }
}
