package com.matzip.common.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MenuDto {
    private Long productId;
    private String name;
    private int price;

}

