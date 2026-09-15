package com.matzip.place.controller;

import com.matzip.common.response.ApiResponse;
import com.matzip.common.security.UserPrincipal;
import com.matzip.place.dto.request.MapSearchRequestDto;
import com.matzip.place.dto.response.PlaceCommonResponseDto;
import jakarta.validation.Valid;
import com.matzip.place.dto.response.MapSearchResponseDto;
import com.matzip.place.dto.response.PlaceDetailResponseDto;
import com.matzip.place.service.PlaceReadService;
import com.matzip.place.domain.Campus;
import com.matzip.place.domain.SortType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "맛집", description = "맛집 조회 API")
@RestController
@RequestMapping("/api/v1/places")
@RequiredArgsConstructor
public class PlaceReadController {

    private final PlaceReadService placeReadService;

    @Operation(summary = "맛집 상세 조회", description = "맛집 상세 정보를 조회한다. 로그인 시 찜 여부(isLiked)가 함께 내려간다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "맛집을 찾을 수 없습니다.")
    @GetMapping("/{placeId}")
    public ApiResponse<PlaceDetailResponseDto> getPlaceDetail(
            @PathVariable Long placeId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        Long userId = userPrincipal != null ? userPrincipal.getUserId() : null;
        PlaceDetailResponseDto placeDetail = placeReadService.getPlaceDetail(placeId, userId);
        
        return ApiResponse.success(placeDetail);
    }

    @Operation(summary = "지도 범위 내 맛집 조회", description = "지도 좌표 범위(bounds) 내의 맛집 목록을 조회한다.")
    @GetMapping
    public ApiResponse<List<MapSearchResponseDto>> getPlacesInMap(
            @Valid @ModelAttribute MapSearchRequestDto requestDto) {

        List<MapSearchResponseDto> places = placeReadService.findPlacesInMapBounds(requestDto);
        return ApiResponse.success(places);
    }

    @Operation(summary = "맛집 랭킹 조회", description = "캠퍼스별로 정렬 기준(좋아요/조회수/최신)에 따른 맛집 랭킹을 조회한다.")
    @GetMapping("/ranking")
    public ApiResponse<List<PlaceCommonResponseDto>> getRanking(
            @RequestParam SortType sort,
            @RequestParam Campus campus) {

        List<PlaceCommonResponseDto> ranking = placeReadService.getRanking(campus, sort);
        return ApiResponse.success(ranking);
    }
}
