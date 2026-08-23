package com.matzip.admin.controller.response;

import com.matzip.place.domain.Campus;
import com.matzip.place.domain.entity.Place;
import com.matzip.place.dto.CategoryDto;
import com.matzip.place.dto.TagDto;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record PlaceRegisterRequestsResponseDto(Long placeId, String placeName, LocalDate requestDate, Campus campus,
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
