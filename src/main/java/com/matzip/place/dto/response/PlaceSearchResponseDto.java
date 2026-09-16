package com.matzip.place.dto.response;

import com.matzip.place.domain.entity.Place;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlaceSearchResponseDto {

    @Schema(description = "맛집 ID", example = "15")
    private Long placeId;

    @Schema(description = "맛집 이름", example = "우돈탄 다산본점")
    private String placeName;

    @Schema(description = "주소", example = "경기 남양주시 다산중앙로82번길 25")
    private String address;

    public static PlaceSearchResponseDto from(Place place) {
        return PlaceSearchResponseDto.builder()
                .placeId(place.getId())
                .placeName(place.getName())
                .address(place.getAddress())
                .build();
    }
}

