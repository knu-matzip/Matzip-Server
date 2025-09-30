package com.matzip.place.api.controller;

import com.matzip.common.response.ApiResponse;
import com.matzip.common.security.UserPrincipal;
import com.matzip.place.api.response.LikedPlaceResponseDto;
import com.matzip.place.application.service.PlaceLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/places")
public class PlaceLikeController {

    private final PlaceLikeService placeLikeService;


    @PostMapping("/{placeId}/like")
    public ResponseEntity<ApiResponse<Void>> addLike(
            @PathVariable Long placeId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        placeLikeService.addLike(userPrincipal.getUserId(), placeId);
        return ResponseEntity.ok(ApiResponse.successWithoutData());
    }

    @DeleteMapping("/{placeId}/like")
    public ResponseEntity<ApiResponse<Void>> removeLike(
            @PathVariable Long placeId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        placeLikeService.removeLike(userPrincipal.getUserId(), placeId);
        return ResponseEntity.ok(ApiResponse.successWithoutData());
    }

    @GetMapping("/like")
    public ResponseEntity<ApiResponse<List<LikedPlaceResponseDto>>> getMyLikedPlaces(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        List<LikedPlaceResponseDto> likedPlaces = placeLikeService.getLikedPlaces(userPrincipal.getUserId());
        return ResponseEntity.ok(ApiResponse.success(likedPlaces));
    }
}
