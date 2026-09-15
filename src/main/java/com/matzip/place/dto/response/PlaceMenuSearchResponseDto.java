package com.matzip.place.dto.response;

import com.matzip.place.domain.entity.Menu;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlaceMenuSearchResponseDto {

    @Schema(description = "맛집 ID", example = "15")
    private Long placeId;

    @Schema(description = "메뉴 이름", example = "숙성우대갈비")
    private String menuName;

    @Schema(description = "맛집 이름", example = "우돈탄 다산본점")
    private String placeName;

    public static PlaceMenuSearchResponseDto from(Menu menu) {
        return PlaceMenuSearchResponseDto.builder()
                .placeId(menu.getPlace().getId())
                .menuName(menu.getName())
                .placeName(menu.getPlace().getName())
                .build();
    }
}
