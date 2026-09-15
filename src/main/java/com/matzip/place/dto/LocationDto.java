package com.matzip.place.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;


@Getter
@Builder
public class LocationDto {

    @Schema(description = "위도", example = "37.625")
    private double latitude;

    @Schema(description = "경도", example = "127.151")
    private double longitude;

    public static LocationDto of(double latitude, double longitude) {
        return LocationDto.builder()
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }
}
