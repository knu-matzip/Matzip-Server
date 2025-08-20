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
public class PlaceListResponseDto {

    private Long placeId;
    private String placeName;
    private String address;
    private List<CategoryDto> categories;
    private List<TagDto> tags;

    // 서비스 계층에서 모든 정보를 조합하여 DTO로 변환
    public static PlaceListResponseDto from(Place place, List<Category> categories, List<Tag> tags) {
        return PlaceListResponseDto.builder()
                .placeId(place.getId())
                .placeName(place.getName())
                .address(place.getAddress())
                .categories(categories.stream().map(CategoryDto::from).collect(Collectors.toList()))
                .tags(tags.stream().map(TagDto::from).collect(Collectors.toList()))
                .build();
    }
}