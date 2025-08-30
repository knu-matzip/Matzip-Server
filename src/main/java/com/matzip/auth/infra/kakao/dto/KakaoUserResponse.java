package com.matzip.auth.infra.kakao.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카카오 사용자 정보 응답 바인딩용 DTO
 */
@Getter
@NoArgsConstructor
public class KakaoUserResponse {

    // 카카오 고유 사용자 ID (Long)
    private Long id;

}
