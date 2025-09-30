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
public class PlaceRegisterResponseDto {

    private Long placeId;
    private String placeName;
    private String address;
    private List<CategoryDto> categories;
    private List<TagDto> tags;

    // 서비스 계층에서 모든 정보를 조합하여 DTO로 변환
    public static PlaceRegisterResponseDto from(Place place, List<Category> categories, List<Tag> tags) {
        return PlaceRegisterResponseDto.builder()
                .placeId(place.getId())
                .placeName(place.getName())
                .address(place.getAddress())
                .categories(categories.stream().map(CategoryDto::from).collect(Collectors.toList()))
                .tags(tags.stream().map(TagDto::from).collect(Collectors.toList()))
                .build();
    }
}