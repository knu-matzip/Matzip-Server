package com.matzip.place.api.response;

import com.matzip.place.domain.entity.Menu;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlaceMenuSearchResponseDto {

    private Long placeId;
    private String menuName;
    private String placeName;

    public static PlaceMenuSearchResponseDto from(Menu menu) {
        return PlaceMenuSearchResponseDto.builder()
                .placeId(menu.getPlace().getId())
                .menuName(menu.getName())
                .placeName(menu.getPlace().getName())
                .build();
    }
}
