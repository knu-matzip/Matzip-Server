package com.matzip.place.api.response;

import com.matzip.place.dto.CategoryDto;
import com.matzip.place.dto.TagDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CategoryPlaceResponseDto {
    private Long placeId;
    private String placeName;
    private String address;
    private List<CategoryDto> categories;
    private List<TagDto> tags;

    public static CategoryPlaceResponseDto of(Long placeId, String placeName, String address,
                                            List<CategoryDto> categories, List<TagDto> tags) {
        return CategoryPlaceResponseDto.builder()
                .placeId(placeId)
                .placeName(placeName)
                .address(address)
                .categories(categories)
                .tags(tags)
                .build();
    }
}
