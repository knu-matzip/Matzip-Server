package com.matzip.place.api.controller;

import com.matzip.common.response.ApiResponse;
import com.matzip.place.api.response.CategoryPlaceResponseDto;
import com.matzip.place.application.service.PlaceReadService;
import com.matzip.place.domain.Campus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final PlaceReadService placeReadService;

    @GetMapping("/{categoryId}/places")
    public ResponseEntity<ApiResponse<List<CategoryPlaceResponseDto>>> getPlacesByCategory(
            @PathVariable Long categoryId,
            @RequestParam Campus campus) {

        List<CategoryPlaceResponseDto> places = placeReadService.getPlacesByCategory(categoryId, campus);
        return ResponseEntity.ok(ApiResponse.success(places));
    }
}
