package com.matzip.place.api.response;

import com.matzip.common.dto.LocationDto;
import com.matzip.common.dto.PhotoDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PlaceCheckResponseDto {

    private Boolean alreadyRegistered; // 우리 서비스에 이미 등록된 가게인지 여부

    // 이미 등록된 경우에만 채움(미등록 프리뷰에선 null)
    private Long placeId;

    private String placeName;
    private String address;
    private LocationDto location;

    // 미등록 프리뷰: 실제 사진/메뉴로 채움
    // 이미 등록: 빈 배열
    private List<PhotoDto> photos;
    private List<MenuItem> menus;

    @Getter
    @Builder
    public static class MenuItem {
        private String name;
        private int price;
        private boolean isRecommended; // 프리뷰 기본값: false
    }



}