package com.matzip.place.infra;

import com.matzip.place.domain.Place;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    // 카카오 플레이스 ID로 맛집이 이미 등록되었는지 확인
    boolean existsByKakaoPlaceId(Long kakaoPlaceId);

    // 카카오 플레이스 ID로 맛집 정보를 조회할 때 사용
    Optional<Place> findByKakaoPlaceId(Long kakaoPlaceId);
}
