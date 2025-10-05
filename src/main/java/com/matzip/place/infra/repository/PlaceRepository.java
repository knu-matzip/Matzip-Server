package com.matzip.place.infra.repository;

import com.matzip.place.domain.Place;
import com.matzip.place.domain.PlaceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    /**
     * 지도 범위 내 맛집 조회 (정렬 없음)
     */
    @Query("SELECT p FROM Place p WHERE p.latitude BETWEEN :minLat AND :maxLat AND p.longitude BETWEEN :minLng AND :maxLng AND p.status = 'APPROVED'")
    List<Place> findWithinBounds(
            @Param("minLat") double minLat, @Param("maxLat") double maxLat,
            @Param("minLng") double minLng, @Param("maxLng") double maxLng
    );

    /**
     * 지도 범위 내 맛집을 조회하고, 사용자 위치 기준으로 가까운 순으로 정렬하는 쿼리
     */
    @Query(value = "SELECT * FROM place p " +
            "WHERE p.latitude BETWEEN :minLat AND :maxLat " +
            "  AND p.longitude BETWEEN :minLng AND :maxLng " +
            "  AND p.status = 'APPROVED' " +
            "ORDER BY ST_DISTANCE_SPHERE(POINT(:userLng, :userLat), POINT(p.longitude, p.latitude)) ASC ",
            nativeQuery = true)
    List<Place> findWithinBoundsAndSortByDistance(
            @Param("minLat") double minLat, @Param("maxLat") double maxLat,
            @Param("minLng") double minLng, @Param("maxLng") double maxLng,
            @Param("userLat") double userLat, @Param("userLng") double userLng
    );

    @Modifying
    @Query("UPDATE Place p SET p.viewCount = p.viewCount + 1 WHERE p.id = :placeId")
    void incrementViewCount(@Param("placeId") Long placeId);
}
