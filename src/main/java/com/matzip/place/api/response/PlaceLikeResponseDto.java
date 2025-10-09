package com.matzip.place.api.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlaceLikeResponseDto {
    private final Long placeId;
    private final String message;

    public static PlaceLikeResponseDto addLike(Long placeId) {
        return PlaceLikeResponseDto.builder()
                .placeId(placeId)
                .message("찜 목록에 추가되었습니다.")
                .build();
    }

    public static PlaceLikeResponseDto removeLike(Long placeId) {
        return PlaceLikeResponseDto.builder()
                .placeId(placeId)
                .message("찜 목록에서 제거되었습니다.")
                .build();
    }
}
