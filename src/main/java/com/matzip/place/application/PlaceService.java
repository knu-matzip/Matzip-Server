package com.matzip.place.application;

import com.matzip.place.api.request.PlaceRequestDto;
import com.matzip.place.api.response.PlaceCheckResponseDto;

import java.util.Optional;

public interface PlaceService {

    /**
     * 등록 전, 가게 정보를 확인하고 중복 여부를 체크 (프리뷰 단계)
     * @param kakaoPlaceId 클라이언트에서 받은 카카오 장소 ID
     * @return 가게 상세 정보 및 등록 여부가 포함된 DTO
     */
    PlaceCheckResponseDto checkPlace(Long kakaoPlaceId);

    /**
     * 새로운 맛집 스팟을 최종 등록합니다.
     * @param requestDto 등록 요청 데이터
     * @param userId 로그인한 사용자의 ID (비로그인 시 Optional.empty())
     * @return 생성된 맛집 스팟의 ID
     */
    Long registerPlace(PlaceRequestDto requestDto, Optional<Long> userId);
}
