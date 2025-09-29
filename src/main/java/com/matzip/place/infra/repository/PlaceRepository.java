package com.matzip.place.infra.repository;

import com.matzip.place.domain.Place;
import com.matzip.place.domain.PlaceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    // 카카오 플레이스 ID로 맛집이 이미 등록되었는지 확인
    boolean existsByKakaoPlaceId(String kakaoPlaceId);

    // 카카오 플레이스 ID로 맛집 정보를 조회할 때 사용
    Optional<Place> findByKakaoPlaceId(String kakaoPlaceId);

    // 상태별 Place 조회 (관리자 기능용)
    List<Place> findByStatus(PlaceStatus status);

    // 등록자별 Place 조회 (사용자가 자신이 등록한 Place 상태 확인용)
    List<Place> findByRegisteredByIdOrderByIdDesc(Long registeredById);

    @Query("SELECT p FROM Place p WHERE p.latitude BETWEEN :minLat AND :maxLat AND p.longitude BETWEEN :minLng AND :maxLng AND p.status = 'APPROVED'")
    List<Place> findWithinBounds(
            @Param("minLat") double minLat, @Param("maxLat") double maxLat,
            @Param("minLng") double minLng, @Param("maxLng") double maxLng
    );
}
