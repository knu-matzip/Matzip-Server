package com.matzip.place.dto.response;

import com.matzip.place.domain.entity.Category;
import com.matzip.place.domain.entity.Menu;
import com.matzip.place.domain.entity.Photo;
import com.matzip.place.domain.entity.Place;
import com.matzip.place.domain.entity.Tag;
import com.matzip.place.dto.CategoryDto;
import com.matzip.place.dto.LocationDto;
import com.matzip.place.dto.PhotoDto;
import com.matzip.place.dto.TagDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class PlaceRegisterStatusDetailResponseDto {

    @Schema(description = "맛집 ID", example = "15")
    private Long placeId;

    @Schema(description = "맛집 이름", example = "우돈탄 다산본점")
    private String placeName;

    @Schema(description = "등록 요청일", example = "2025-08-12")
    private LocalDate requestDate;

    private List<PhotoDto> photos;

    @Schema(description = "주소", example = "경기 남양주시 다산중앙로82번길 25")
    private String address;

    private LocationDto location;

    @Schema(description = "맛집 설명", example = "직원이 엄청 친절해요! 근데 화장실에 갔고 냄새나요 ㅠㅠㅠ 그래도 맛은 있어서 괜찮아요")
    private String description;

    private List<MenuResponseDto> menus;
    private List<CategoryDto> categories;
    private List<TagDto> tags;

    @Schema(description = "등록 상태", example = "REJECTED")
    private String registerStatus;

    @Schema(description = "거절 사유(거절 시)", example = "메뉴 정보가 부족합니다.")
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
