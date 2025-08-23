package com.matzip.common.dto;

import lombok.Builder;
import lombok.Getter;
import com.matzip.place.domain.Menu;

@Getter
@Builder
public class MenuDto {
    private String name;
    private int price;

    public static MenuDto from(Menu menu) {
        return MenuDto.builder()
                .name(menu.getName())
                .price(menu.getPrice())
                .build();
    }
}

