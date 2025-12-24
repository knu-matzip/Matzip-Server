package com.matzip.place.api.request;

import com.matzip.place.domain.Campus;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PlaceRequestDto {

    @NotNull(message = "카카오 장소 ID는 필수입니다.")
    private String kakaoPlaceId;

    @NotNull(message = "캠퍼스 정보는 필수입니다.")
    private Campus campus;

    @NotBlank
    @Size(min = 1, max = 1000, message = "설명은 1자 이상 1000자 이하로 작성되어야 합니다.")
    private String description;

    @NotEmpty(message = "메뉴 정보는 최소 1개 이상이어야 합니다.")
    private List<MenuInfo> menus;

    @Size(max = 6, message = "태그는 최대 6개까지 선택할 수 있습니다.")
    private List<Long> tagIds;

    @NotEmpty(message = "카테고리는 최소 1개 이상 선택해야 합니다.")
    @Size(max = 5, message = "카테고리는 최대 5개까지 선택할 수 있습니다.")
    private List<Long> categoryIds;

    private Long registeredBy; // 등록자 ID (nullable)

    // 메뉴 정보를 담는 내부 DTO
    @Getter
    @Setter
    @NoArgsConstructor
    public static class MenuInfo {
        private Long menuId;

        @NotEmpty(message = "메뉴 이름은 필수입니다.")
        private String name;

        @NotNull(message = "메뉴 가격은 필수입니다.")
        @PositiveOrZero(message = "메뉴 가격은 0 이상이어야 합니다.")
        private Integer price;

        @NotNull(message = "추천 여부는 필수입니다.")
        private Boolean isRecommended;
    }
}
