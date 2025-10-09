package com.matzip.place.api.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 프리뷰 요청용
 */
@Getter
@Setter
@NoArgsConstructor
public class PlaceCheckRequestDto {

    @NotNull
    private String kakaoPlaceId;

}
