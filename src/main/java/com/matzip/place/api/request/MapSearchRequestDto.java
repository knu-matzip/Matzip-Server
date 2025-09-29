package com.matzip.place.api.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MapSearchRequestDto {

    private Double minLat;
    private Double maxLat;
    private Double minLng;
    private Double maxLng;
}
