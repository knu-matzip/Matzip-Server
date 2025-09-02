package com.matzip.common.dto;

import lombok.*;


@Getter
@Builder
public class LocationDto {

    private double latitude;
    private double longitude;

    public static LocationDto of(double latitude, double longitude) {
        return LocationDto.builder()
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }
}
