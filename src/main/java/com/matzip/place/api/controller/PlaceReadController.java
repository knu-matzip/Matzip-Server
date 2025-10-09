package com.matzip.place.api.controller;

import com.matzip.common.response.ApiResponse;
import com.matzip.common.security.UserPrincipal;
import com.matzip.place.api.request.MapSearchRequestDto;
import jakarta.validation.Valid;
import com.matzip.place.api.response.CategoryPlaceResponseDto;
import com.matzip.place.api.response.MapSearchResponseDto;
import com.matzip.place.api.response.PlaceDetailResponseDto;
import com.matzip.place.api.response.PlaceRankingResponseDto;
import com.matzip.place.application.service.PlaceReadService;
import com.matzip.place.domain.Campus;
import com.matzip.place.domain.SortType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/places")
@RequiredArgsConstructor
public class PlaceReadController {

    private final PlaceReadService placeReadService;

    @GetMapping("/{placeId}")
    public ResponseEntity<ApiResponse<PlaceDetailResponseDto>> getPlaceDetail(
            @PathVariable Long placeId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        Long userId = userPrincipal != null ? userPrincipal.getUserId() : null;
        PlaceDetailResponseDto placeDetail = placeReadService.getPlaceDetail(placeId, userId);
        
        return ResponseEntity.ok(ApiResponse.success(placeDetail));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MapSearchResponseDto>>> getPlacesInMap(
            @Valid @ModelAttribute MapSearchRequestDto requestDto) {

        List<MapSearchResponseDto> places = placeReadService.findPlacesInMapBounds(requestDto);
        return ResponseEntity.ok(ApiResponse.success(places));
    }

    @GetMapping("/category")
    public ResponseEntity<ApiResponse<List<CategoryPlaceResponseDto>>> getPlacesByCategory(
            @RequestParam Long categoryId,
            @RequestParam Campus campus) {

        List<CategoryPlaceResponseDto> places = placeReadService.getPlacesByCategory(categoryId, campus);
        return ResponseEntity.ok(ApiResponse.success(places));
    }

    @GetMapping("/ranking")
    public ResponseEntity<ApiResponse<List<PlaceRankingResponseDto>>> getRanking(
            @RequestParam SortType sort,
            @RequestParam Campus campus) {

        List<PlaceRankingResponseDto> ranking = placeReadService.getRanking(campus, sort);
        return ResponseEntity.ok(ApiResponse.success(ranking));
    }
}
