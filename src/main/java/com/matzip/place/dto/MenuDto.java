package com.matzip.place.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MenuDto {
    private Long menuId;
    private String name;
    private int price;

}

