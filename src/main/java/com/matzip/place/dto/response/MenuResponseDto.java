package com.matzip.place.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.matzip.place.domain.entity.Menu;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MenuResponseDto {

    @Schema(description = "메뉴 ID", example = "202")
    private Long menuId;

    @Schema(description = "메뉴 이름", example = "숙성우대갈비")
    private String name;

    @Schema(description = "가격(원)", example = "32000")
    private int price;

    @Schema(description = "대표 메뉴 여부", example = "true")
    @Getter(onMethod_ = @JsonProperty("isRecommended"))
    private boolean isRecommended;

    public static MenuResponseDto from(Menu menu) {
        return MenuResponseDto.builder()
                .menuId(menu.getId())
                .name(menu.getName())
                .price(menu.getPrice())
                .isRecommended(menu.isRecommended())
                .build();
    }
}