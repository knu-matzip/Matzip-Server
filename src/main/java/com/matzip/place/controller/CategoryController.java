package com.matzip.place.controller;

import com.matzip.common.response.ApiResponse;
import com.matzip.place.dto.response.PlaceCommonResponseDto;
import com.matzip.place.service.PlaceReadService;
import com.matzip.place.domain.Campus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.matzip.place.service.CategoryService;
import com.matzip.place.dto.CategoryDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "카테고리", description = "카테고리 및 카테고리별 맛집 조회 API")
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final PlaceReadService placeReadService;
    private final CategoryService categoryService;

    @Operation(summary = "카테고리별 맛집 목록", description = "특정 카테고리에 속한 캠퍼스별 맛집 목록을 조회한다.")
    @GetMapping("/{categoryId}/places")
    public ApiResponse<List<PlaceCommonResponseDto>> getPlacesByCategory(
            @PathVariable Long categoryId,
            @RequestParam Campus campus) {

        List<PlaceCommonResponseDto> places = placeReadService.getPlacesByCategory(categoryId, campus);
        return ApiResponse.success(places);
    }

    @Operation(summary = "전체 카테고리 목록", description = "등록에 사용할 수 있는 전체 카테고리 목록을 조회한다.")
    @GetMapping
    public ApiResponse<List<CategoryDto>> getAllCategories() {
        List<CategoryDto> categories = categoryService.getAllCategories();
        return ApiResponse.success(categories);
    }
}
