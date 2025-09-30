package com.matzip.place.api.response;

import com.matzip.common.dto.*;
import com.matzip.place.api.RecommendedMenuDto;
import com.matzip.place.domain.entity.*;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class PlaceDetailResponseDto {

    private Long placeId;
    private String placeName;
    private String address;
    private LocationDto location;
    private List<PhotoDto> photos;
    private boolean isLiked;
    private String description;
    private List<RecommendedMenuDto> menus;
    private List<TagDto> tags;
    private List<CategoryDto> categories;

    // 서비스 계층에서 모든 정보를 조합하여 DTO로 변환하는 정적 팩토리 메서드
    public static PlaceDetailResponseDto from(
            Place place,
            List<Photo> photos,
            List<Menu> menus,
            List<Category> categories,
            List<Tag> tags,
            boolean isLiked
    ) {
        return PlaceDetailResponseDto.builder()
                .placeId(place.getId())
                .placeName(place.getName())
                .address(place.getAddress())
                .description(place.getDescription())
                .location(LocationDto.of(place.getLatitude(), place.getLongitude()))
                .photos(photos.stream().map(PhotoDto::from).collect(Collectors.toList()))
                .menus(menus.stream().map(RecommendedMenuDto::from).collect(Collectors.toList()))
                .categories(categories.stream().map(CategoryDto::from).collect(Collectors.toList()))
                .tags(tags.stream().map(TagDto::from).collect(Collectors.toList()))
                .isLiked(isLiked)
                .build();
    }
}