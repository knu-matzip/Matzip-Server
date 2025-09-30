package com.matzip.place.api.response;

import com.matzip.common.dto.CategoryDto;
import com.matzip.common.dto.TagDto;
import com.matzip.place.domain.Category;
import com.matzip.place.domain.Place;
import com.matzip.place.domain.Tag;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class LikedPlaceResponseDto {
    private final Long placeId;
    private final String placeName;
    private final String address;
    private final List<CategoryDto> categories;
    private final List<TagDto> tags;

    public static LikedPlaceResponseDto from(Place place, List<Category> categoryEntities, List<Tag> tagEntities) {

        List<CategoryDto> categoryDtos = categoryEntities.stream()
                .map(CategoryDto::from)
                .collect(Collectors.toList());

        List<TagDto> tagDtos = tagEntities.stream()
                .map(TagDto::from)
                .collect(Collectors.toList());

        return LikedPlaceResponseDto.builder()
                .placeId(place.getId())
                .placeName(place.getName())
                .address(place.getAddress())
                .categories(categoryDtos)
                .tags(tagDtos)
                .build();
    }

}
