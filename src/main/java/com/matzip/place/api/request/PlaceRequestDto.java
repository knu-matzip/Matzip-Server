package com.matzip.place.api.request;

import com.matzip.common.dto.LocationDto;
import com.matzip.place.domain.Campus;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PlaceRequestDto {

    @NotNull(message = "카카오 장소 ID는 필수입니다.")
    private String kakaoPlaceId;

    @NotNull(message = "캠퍼스 정보는 필수입니다.")
    private Campus campus;

    @NotEmpty(message = "가게 이름은 필수입니다.")
    private String name;

    @NotEmpty(message = "주소는 필수입니다.")
    private String address;

    @NotNull(message = "위치 정보는 필수입니다.")
    private LocationDto location;

    private String description;

    @NotEmpty(message = "메뉴 정보는 최소 1개 이상이어야 합니다.")
    private List<MenuInfo> menus;

    @Size(max = 3, message = "태그는 최대 3개까지 선택할 수 있습니다.")
    private List<Long> tagIds;

    @NotEmpty(message = "카테고리는 최소 1개 이상 선택해야 합니다.")
    @Size(max = 5, message = "카테고리는 최대 5개까지 선택할 수 있습니다.")
    private List<Long> categoryIds;

    private Long registeredBy; // 등록자 ID (nullable)

    // 메뉴 정보를 담는 내부 DTO
    @Getter
    @NoArgsConstructor
    public static class MenuInfo {
        @NotEmpty(message = "메뉴 이름은 필수입니다.")
        private String name;

        @NotNull(message = "메뉴 가격은 필수입니다.")
        @PositiveOrZero(message = "메뉴 가격은 0 이상이어야 합니다.")
        private Integer price;

        @NotNull(message = "추천 여부는 필수입니다.")
        private Boolean isRecommended;
    }
}
