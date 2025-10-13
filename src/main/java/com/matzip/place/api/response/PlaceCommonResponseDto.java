package com.matzip.place.api.response;

import com.matzip.place.dto.CategoryDto;
import com.matzip.place.dto.TagDto;
import com.matzip.place.domain.entity.Category;
import com.matzip.place.domain.entity.Place;
import com.matzip.place.domain.entity.Tag;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class PlaceCommonResponseDto {
    private final Long placeId;
    private final String placeName;
    private final String address;
    private final List<CategoryDto> categories;
    private final List<TagDto> tags;

    public static PlaceCommonResponseDto from(Place place, List<Category> categoryEntities, List<Tag> tagEntities) {
        List<CategoryDto> categoryDtos = categoryEntities.stream()
                .map(CategoryDto::from)
                .collect(Collectors.toList());

        List<TagDto> tagDtos = tagEntities.stream()
                .map(TagDto::from)
                .collect(Collectors.toList());

        return PlaceCommonResponseDto.builder()
                .placeId(place.getId())
                .placeName(place.getName())
                .address(place.getAddress())
                .categories(categoryDtos)
                .tags(tagDtos)
                .build();
    }
}

