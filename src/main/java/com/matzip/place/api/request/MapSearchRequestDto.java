package com.matzip.place.api.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MapSearchRequestDto {

    @NotNull(message = "최소 위도는 필수입니다.")
    private Double minLat;

    @NotNull(message = "최대 위도는 필수입니다.")
    private Double maxLat;

    @NotNull(message = "최소 경도는 필수입니다.")
    private Double minLng;

    @NotNull(message = "최대 경도는 필수입니다.")
    private Double maxLng;

    private Double userLat;
    private Double userLng;
}
