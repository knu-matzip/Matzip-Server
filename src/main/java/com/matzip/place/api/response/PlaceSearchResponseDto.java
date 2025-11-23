package com.matzip.place.api.response;

import com.matzip.place.domain.entity.Place;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlaceSearchResponseDto {

    private Long placeId;
    private String placeName;
    private String address;

    public static PlaceSearchResponseDto from(Place place) {
        return PlaceSearchResponseDto.builder()
                .placeId(place.getId())
                .placeName(place.getName())
                .address(place.getAddress())
                .build();
    }
}

