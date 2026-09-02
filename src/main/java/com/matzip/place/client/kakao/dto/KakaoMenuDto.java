package com.matzip.place.client.kakao.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KakaoMenuDto {
    private Long menuId;
    private String name;
    private int price;

}

