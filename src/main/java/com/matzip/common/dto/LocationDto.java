package com.matzip.common.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class LocationDto {

    private BigDecimal latitude;
    private BigDecimal longitude;

    public static LocationDto of(BigDecimal latitude, BigDecimal longitude) {
        return LocationDto.builder()
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }
}
