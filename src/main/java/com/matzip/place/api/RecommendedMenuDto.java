package com.matzip.place.api;

import com.matzip.place.domain.Menu;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecommendedMenuDto {
    private String name;
    private int price;
    private boolean isRecommended;

    public static RecommendedMenuDto from(Menu menu) {
        return RecommendedMenuDto.builder()
                .name(menu.getName())
                .price(menu.getPrice())
                .isRecommended(menu.getIsRecommended())
                .build();
    }
}