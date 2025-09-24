package com.matzip.place.api;

import com.matzip.common.response.ApiResponse;
import com.matzip.place.api.response.PlaceDetailResponseDto;
import com.matzip.place.application.PlaceReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/places")
@RequiredArgsConstructor
public class PlaceReadController {

    private final PlaceReadService placeReadService;

    /**
     * 맛집 상세 정보 조회
     * @param placeId 조회할 맛집 ID
     * @param userId 현재 사용자 ID (좋아요 여부 확인용)
     * @return 맛집 상세 정보
     */
    @GetMapping("/{placeId}")
    public ResponseEntity<ApiResponse<PlaceDetailResponseDto>> getPlaceDetail(
            @PathVariable Long placeId,
            @RequestParam(required = false) Long userId) {
        
        PlaceDetailResponseDto placeDetail = placeReadService.getPlaceDetail(placeId, userId);
        
        return ResponseEntity.ok(ApiResponse.success(placeDetail));
    }
}
