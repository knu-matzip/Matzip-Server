package com.matzip.place.dto.response;

import com.matzip.place.dto.CategoryDto;
import com.matzip.place.dto.LocationDto;
import com.matzip.place.dto.PhotoDto;
import com.matzip.place.dto.TagDto;
import com.matzip.place.domain.entity.Category;
import com.matzip.place.domain.entity.Photo;
import com.matzip.place.domain.entity.Place;
import com.matzip.place.domain.entity.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class MapSearchResponseDto {

    @Schema(description = "맛집 ID", example = "15")
    private Long placeId;

    @Schema(description = "맛집 이름", example = "우돈탄 다산본점")
    private String placeName;

    @Schema(description = "주소", example = "경기 남양주시 다산중앙로82번길 25")
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
