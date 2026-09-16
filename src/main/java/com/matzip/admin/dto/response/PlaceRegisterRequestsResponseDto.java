package com.matzip.admin.dto.response;

import com.matzip.place.domain.Campus;
import com.matzip.place.domain.entity.Place;
import com.matzip.place.dto.CategoryDto;
import com.matzip.place.dto.TagDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record PlaceRegisterRequestsResponseDto(
        @Schema(description = "맛집 ID", example = "15") Long placeId,
        @Schema(description = "맛집 이름", example = "우돈탄 다산본점") String placeName,
        @Schema(description = "등록 요청일", example = "2025-08-12") LocalDate requestDate,
        @Schema(description = "캠퍼스", example = "SINGWAN") Campus campus,
        List<CategoryDto> categories, List<TagDto> tags) {

    public static PlaceRegisterRequestsResponseDto from(Place place) {
        return PlaceRegisterRequestsResponseDto.builder()
                .placeId(place.getId())
                .placeName(place.getName())
                .requestDate(place.getCreatedAt().toLocalDate())
                .campus(place.getCampus())
                .categories(place.getCategories()
                        .stream()
                        .map(CategoryDto::from)
                        .toList())
                .tags(place.getTags()
                        .stream()
                        .map(TagDto::from)
                        .toList())
                .build();
    }
}
