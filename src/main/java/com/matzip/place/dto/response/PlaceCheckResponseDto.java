package com.matzip.place.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.matzip.place.dto.LocationDto;
import com.matzip.place.dto.PhotoDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PlaceCheckResponseDto {

    @Schema(description = "이미 등록된 가게인지 여부", example = "false")
    private Boolean alreadyRegistered; // 우리 서비스에 이미 등록된 가게인지 여부

    @Schema(description = "맛집 이름", example = "우돈탄 다산본점")
    private String placeName;

    @Schema(description = "주소", example = "경기 남양주시 다산중앙로82번길 25")
    private String address;

    private LocationDto location;

    // 미등록 프리뷰: 실제 사진/메뉴로 채움
    // 이미 등록: 빈 배열
    private List<PhotoDto> photos;
    private List<MenuItem> menus;

    @Getter
    @Builder
    public static class MenuItem {

        @Schema(description = "메뉴 ID", example = "202")
        private Long menuId;

        @Schema(description = "메뉴 이름", example = "숙성우대갈비")
        private String name;

        @Schema(description = "가격(원)", example = "32000")
        private int price;

        @Schema(description = "대표 메뉴 여부", example = "true")
        @Getter(onMethod_ = @JsonProperty("isRecommended"))
        private boolean isRecommended; // 프리뷰 기본값: false
    }



}