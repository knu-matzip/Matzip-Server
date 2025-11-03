package com.matzip.place.api.controller;

import com.matzip.common.response.ApiResponse;
import com.matzip.place.api.response.PlaceCommonResponseDto;
import com.matzip.place.application.service.PlaceReadService;
import com.matzip.place.domain.Campus;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.matzip.place.application.service.CategoryService;
import com.matzip.place.dto.CategoryDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final PlaceReadService placeReadService;
    private final CategoryService categoryService;

    @GetMapping("/{categoryId}/places")
    public ApiResponse<List<PlaceCommonResponseDto>> getPlacesByCategory(
            @PathVariable Long categoryId,
            @RequestParam Campus campus) {

        List<PlaceCommonResponseDto> places = placeReadService.getPlacesByCategory(categoryId, campus);
        return ApiResponse.success(places);
    }

    @GetMapping
    public ApiResponse<List<CategoryDto>> getAllCategories() {
        List<CategoryDto> categories = categoryService.getAllCategories();
        return ApiResponse.success(categories);
    }
}
