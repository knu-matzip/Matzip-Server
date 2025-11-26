package com.matzip.place.api.response;

import com.matzip.place.domain.entity.Category;
import com.matzip.place.domain.entity.Menu;
import com.matzip.place.domain.entity.Photo;
import com.matzip.place.domain.entity.Place;
import com.matzip.place.domain.entity.Tag;
import com.matzip.place.dto.CategoryDto;
import com.matzip.place.dto.LocationDto;
import com.matzip.place.dto.PhotoDto;
import com.matzip.place.dto.TagDto;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class PlaceRegisterStatusDetailResponseDto {

    private Long placeId;
    private String placeName;
    private LocalDate requestDate;
    private List<PhotoDto> photos;
    private String address;
    private LocationDto location;
    private String description;
    private List<MenuResponseDto> menus;
    private List<CategoryDto> categories;
    private List<TagDto> tags;
    private String registerStatus;
    private String rejectedReason;

    public static PlaceRegisterStatusDetailResponseDto from(Place place,
                                                            List<Photo> photos,
                                                            List<Menu> menus,
                                                            List<Category> categories,
                                                            List<Tag> tags,
                                                            String rejectedReason) {
        return PlaceRegisterStatusDetailResponseDto.builder()
                .placeId(place.getId())
                .placeName(place.getName())
                .requestDate(place.getCreatedAt().toLocalDate())
                .photos(photos.stream().map(PhotoDto::from).collect(Collectors.toList()))
                .address(place.getAddress())
                .location(LocationDto.of(place.getLatitude(), place.getLongitude()))
                .description(place.getDescription())
                .menus(menus.stream()
                        .map(MenuResponseDto::from)
                        .toList())
                .categories(categories.stream()
                        .map(CategoryDto::from)
                        .toList())
                .tags(tags.stream()
                        .map(TagDto::from)
                        .toList())
                .registerStatus(place.getStatus().name())
                .rejectedReason(rejectedReason)
                .build();
    }
}
