package com.matzip.place.infra.kakao.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KakaoMenuDto {
    private Long menuId;
    private String name;
    private int price;

}

