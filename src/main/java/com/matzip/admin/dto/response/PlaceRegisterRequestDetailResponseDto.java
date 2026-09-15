package com.matzip.admin.dto.response;

import com.matzip.place.domain.entity.Menu;
import com.matzip.place.domain.entity.Place;
import com.matzip.place.dto.CategoryDto;
import com.matzip.place.dto.LocationDto;
import com.matzip.place.dto.PhotoDto;
import com.matzip.place.dto.TagDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record PlaceRegisterRequestDetailResponseDto(
        @Schema(description = "맛집 ID", example = "15") Long placeId,
        @Schema(description = "맛집 이름", example = "우돈탄 다산본점") String placeName,
        @Schema(description = "등록 요청일", example = "2025-08-12") LocalDate requestDate,
        List<PhotoDto> photos,
        @Schema(description = "주소", example = "경기 남양주시 다산중앙로82번길 25") String address,
        LocationDto location,
        @Schema(description = "맛집 설명", example = "직원이 엄청 친절해요! 근데 화장실에 갔고 냄새나요 ㅠㅠㅠ 그래도 맛은 있어서 괜찮아요") String description,
        List<MenusResponse> menus, List<CategoryDto> categories, List<TagDto> tags) {

    @Builder
    public record MenusResponse(
            @Schema(description = "메뉴 이름", example = "숙성우대갈비") String name,
            @Schema(description = "가격(원)", example = "32000") Integer price,
            @Schema(description = "대표 메뉴 여부", example = "true") Boolean isRecommended
    ) {
        public static MenusResponse from(Menu menu) {
            return MenusResponse.builder()
                    .name(menu.getName())
                    .price(menu.getPrice())
                    .isRecommended(menu.isRecommended())
                    .build();
        }
    }

    public static PlaceRegisterRequestDetailResponseDto from(Place place) {
        return PlaceRegisterRequestDetailResponseDto.builder()
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
