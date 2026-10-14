package com.matzip.place.dto;

/**
 * 특정 날짜의 맛집별 조회수 합계. 원본 로그와 일별 집계를 같은 형태로 비교하려고 쓴다.
 */
public record PlaceViewTallyDto(Long placeId, Long count) {
}
