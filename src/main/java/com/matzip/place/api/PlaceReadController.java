package com.matzip.place.api;

import com.matzip.common.response.ApiResponse;
import com.matzip.common.security.UserPrincipal;
import com.matzip.place.api.request.MapSearchRequestDto;
import com.matzip.place.api.response.MapSearchResponseDto;
import com.matzip.place.api.response.PlaceDetailResponseDto;
import com.matzip.place.application.PlaceReadService;
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

    /**
     * 맛집 상세 정보 조회
     */
    @GetMapping("/{placeId}")
    public ResponseEntity<ApiResponse<PlaceDetailResponseDto>> getPlaceDetail(
            @PathVariable Long placeId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        Long userId = userPrincipal != null ? userPrincipal.getUserId() : null;
        PlaceDetailResponseDto placeDetail = placeReadService.getPlaceDetail(placeId, userId);
        
        return ResponseEntity.ok(ApiResponse.success(placeDetail));
    }

    /**
     * 지도 범위 내 맛집 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<MapSearchResponseDto>>> getPlacesInMap(
            @ModelAttribute MapSearchRequestDto requestDto) {

        List<MapSearchResponseDto> places = placeReadService.findPlacesInMapBounds(requestDto);
        return ResponseEntity.ok(ApiResponse.success(places));
    }
}
