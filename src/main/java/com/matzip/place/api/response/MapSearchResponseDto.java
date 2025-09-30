package com.matzip.place.api.response;

import com.matzip.common.dto.CategoryDto;
import com.matzip.common.dto.LocationDto;
import com.matzip.common.dto.PhotoDto;
import com.matzip.common.dto.TagDto;
import com.matzip.place.domain.entity.Category;
import com.matzip.place.domain.entity.Photo;
import com.matzip.place.domain.entity.Place;
import com.matzip.place.domain.entity.Tag;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class MapSearchResponseDto {

    private Long placeId;
    private String placeName;
    private String address;
    private LocationDto location;
    private List<PhotoDto> photos;
    private List<CategoryDto> categories;
    private List<TagDto> tags;

    public static MapSearchResponseDto from(Place place, List<Photo> photos, List<Category> categories, List<Tag> tags) {
        return MapSearchResponseDto.builder()
                .placeId(place.getId())
                .placeName(place.getName())
                .address(place.getAddress())
                .location(LocationDto.of(place.getLatitude(), place.getLongitude()))
                .photos(photos.stream()
                        .map(PhotoDto::from)
                        .collect(Collectors.toList()))
                .categories(categories.stream()
                        .map(CategoryDto::from)
                        .collect(Collectors.toList()))
                .tags(tags.stream()
                        .map(TagDto::from)
                        .collect(Collectors.toList()))
                .build();
    }
}
