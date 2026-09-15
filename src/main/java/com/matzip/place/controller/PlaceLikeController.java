package com.matzip.place.controller;

import com.matzip.common.response.ApiResponse;
import com.matzip.common.security.UserPrincipal;
import com.matzip.place.dto.response.PlaceLikeResponseDto;
import com.matzip.place.dto.response.PlaceCommonResponseDto;
import com.matzip.place.service.PlaceLikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "맛집 찜", description = "맛집 찜 추가·취소·목록 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/places")
public class PlaceLikeController {

    private final PlaceLikeService placeLikeService;


    @Operation(summary = "맛집 찜 추가", description = "맛집을 찜 목록에 추가한다. 인증 필요.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증이 필요합니다.")
    @PostMapping("/{placeId}/like")
    public ApiResponse<PlaceLikeResponseDto> addLike(
            @PathVariable Long placeId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        PlaceLikeResponseDto response = placeLikeService.addLike(userPrincipal.getUserId(), placeId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "맛집 찜 취소", description = "맛집을 찜 목록에서 제거한다. 인증 필요.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증이 필요합니다.")
    @DeleteMapping("/{placeId}/like")
    public ApiResponse<PlaceLikeResponseDto> removeLike(
            @PathVariable Long placeId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        PlaceLikeResponseDto response = placeLikeService.removeLike(userPrincipal.getUserId(), placeId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "내가 찜한 맛집 목록", description = "인증된 사용자가 찜한 맛집 목록을 조회한다. 인증 필요.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증이 필요합니다.")
    @GetMapping("/like")
    public ApiResponse<List<PlaceCommonResponseDto>> getMyLikedPlaces(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        List<PlaceCommonResponseDto> likedPlaces = placeLikeService.getLikedPlaces(userPrincipal.getUserId());
        return ApiResponse.success(likedPlaces);
    }
}
