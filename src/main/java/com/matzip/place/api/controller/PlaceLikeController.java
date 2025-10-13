package com.matzip.place.api.controller;

import com.matzip.common.response.ApiResponse;
import com.matzip.common.security.UserPrincipal;
import com.matzip.place.api.response.LikedPlaceResponseDto;
import com.matzip.place.api.response.PlaceLikeResponseDto;
import com.matzip.place.application.service.PlaceLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/places")
public class PlaceLikeController {

    private final PlaceLikeService placeLikeService;


    @PostMapping("/{placeId}/like")
    public ApiResponse<PlaceLikeResponseDto> addLike(
            @PathVariable Long placeId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        PlaceLikeResponseDto response = placeLikeService.addLike(userPrincipal.getUserId(), placeId);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{placeId}/like")
    public ApiResponse<PlaceLikeResponseDto> removeLike(
            @PathVariable Long placeId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        PlaceLikeResponseDto response = placeLikeService.removeLike(userPrincipal.getUserId(), placeId);
        return ApiResponse.success(response);
    }

    @GetMapping("/like")
    public ApiResponse<List<LikedPlaceResponseDto>> getMyLikedPlaces(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        List<LikedPlaceResponseDto> likedPlaces = placeLikeService.getLikedPlaces(userPrincipal.getUserId());
        return ApiResponse.success(likedPlaces);
    }
}
