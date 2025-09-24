package com.matzip.place.api;

import com.matzip.common.response.ApiResponse;
import com.matzip.common.security.UserPrincipal;
import com.matzip.place.api.response.PlaceDetailResponseDto;
import com.matzip.place.application.PlaceReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/places")
@RequiredArgsConstructor
public class PlaceReadController {

    private final PlaceReadService placeReadService;

    /**
     * 맛집 상세 정보 조회
     * @param placeId 조회할 맛집 ID
     * @param userPrincipal 현재 인증된 사용자 정보 (선택적)
     * @return 맛집 상세 정보
     */
    @GetMapping("/{placeId}")
    public ResponseEntity<ApiResponse<PlaceDetailResponseDto>> getPlaceDetail(
            @PathVariable Long placeId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        Long userId = userPrincipal != null ? userPrincipal.getUserId() : null;
        PlaceDetailResponseDto placeDetail = placeReadService.getPlaceDetail(placeId, userId);
        
        return ResponseEntity.ok(ApiResponse.success(placeDetail));
    }
}
