package com.matzip.place.api.response;

import com.matzip.common.dto.LocationDto;
import com.matzip.common.dto.MenuDto;
import com.matzip.common.dto.PhotoDto;
import com.matzip.place.domain.Campus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PlaceCheckResponseDto {

    private Boolean alreadyRegistered; // 우리 서비스에 이미 등록된 가게인지 여부
    private Long kakaoPlaceId;
    private Campus campus;
    private PlaceInfo place;
    private List<MenuDto> menus; // 카카오맵에 등록된 메뉴 정보

    // 가게 기본 정보
    @Getter
    @Builder
    public static class PlaceInfo {
        private String name;
        private String address;
        private LocationDto location;
        private List<PhotoDto> photos; // 카카오맵에 등록된 사진 정보
    }

}