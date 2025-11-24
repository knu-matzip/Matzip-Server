package com.matzip.admin.controller.response;

import com.matzip.place.domain.entity.Menu;
import com.matzip.place.domain.entity.Place;
import com.matzip.place.dto.CategoryDto;
import com.matzip.place.dto.LocationDto;
import com.matzip.place.dto.PhotoDto;
import com.matzip.place.dto.TagDto;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record PlaceRegisterRequestDetailResponse(Long placeId, String placeName, LocalDate requestDate, List<PhotoDto> photos,
                                                 String address, LocationDto location, String description,
                                                 List<MenusResponse> menus, List<CategoryDto> categories, List<TagDto> tags) {

    @Builder
    public record MenusResponse(
            String name,
            Integer price,
            Boolean isRecommended
    ) {
        public static MenusResponse from(Menu menu) {
            return MenusResponse.builder()
                    .name(menu.getName())
                    .price(menu.getPrice())
                    .isRecommended(menu.isRecommended())
                    .build();
        }
    }

    public static PlaceRegisterRequestDetailResponse from(Place place) {
        return PlaceRegisterRequestDetailResponse.builder()
                .placeId(place.getId())
                .placeName(place.getName())
                .requestDate(place.getCreatedAt().toLocalDate())
                .photos(place.getPhotos().stream()
                        .map(PhotoDto::from)
                        .toList())
                .address(place.getAddress())
                .location(LocationDto.of(place.getLatitude(), place.getLongitude()))
                .description(place.getDescription())
                .menus(place.getMenus().stream()
                        .map(MenusResponse::from)
                        .toList())
                .categories(place.getCategories().stream()
                        .map(CategoryDto::from)
                        .toList())
                .tags(place.getTags().stream()
                        .map(TagDto::from)
                        .toList())
                .build();
    }
}
