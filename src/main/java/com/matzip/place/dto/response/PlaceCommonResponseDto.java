package com.matzip.place.dto.response;

import com.matzip.place.dto.CategoryDto;
import com.matzip.place.dto.TagDto;
import com.matzip.place.domain.entity.Category;
import com.matzip.place.domain.entity.Place;
import com.matzip.place.domain.entity.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class PlaceCommonResponseDto {

    @Schema(description = "맛집 ID", example = "15")
    private final Long placeId;

    @Schema(description = "맛집 이름", example = "우돈탄 다산본점")
    private final String placeName;

    @Schema(description = "주소", example = "경기 남양주시 다산중앙로82번길 25")
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

    public static PlaceCommonResponseDto of(Long placeId, String placeName, String address,
                                              List<CategoryDto> categories, List<TagDto> tags) {
        return PlaceCommonResponseDto.builder()
                .placeId(placeId)
                .placeName(placeName)
                .address(address)
                .categories(categories)
                .tags(tags)
                .build();
    }
}

