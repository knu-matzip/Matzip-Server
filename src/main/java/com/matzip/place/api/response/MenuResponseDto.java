package com.matzip.place.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.matzip.place.domain.entity.Menu;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MenuResponseDto {
    private Long menuId;
    private String name;
    private int price;

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