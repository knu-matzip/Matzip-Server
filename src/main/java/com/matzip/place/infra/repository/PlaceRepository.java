package com.matzip.place.infra.repository;

import com.matzip.place.domain.Campus;
import com.matzip.place.domain.PlaceStatus;
import com.matzip.place.domain.entity.Place;
import com.matzip.user.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    boolean existsByKakaoPlaceIdAndStatus(String kakaoPlaceId, PlaceStatus status);

    Optional<Place> findByKakaoPlaceIdAndStatus(String kakaoPlaceId, PlaceStatus status);

    @Query("SELECT p FROM Place p WHERE p.latitude BETWEEN :minLat AND :maxLat AND p.longitude BETWEEN :minLng AND :maxLng AND p.status = 'APPROVED'")
    List<Place> findWithinBounds(
            @Param("minLat") double minLat, @Param("maxLat") double maxLat,
            @Param("minLng") double minLng, @Param("maxLng") double maxLng
    );

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

    @EntityGraph(attributePaths = {"placeCategories", "placeCategories.category", "placeTags", "placeTags.tag"})
    @Query("SELECT DISTINCT place FROM Place place WHERE place.status = 'PENDING'")
    List<Place> findPendingPlaces();

    @EntityGraph(attributePaths = {"placeCategories", "placeCategories.category", "placeTags", "placeTags.tag"})
    @Query("SELECT DISTINCT place FROM Place place WHERE place.id = :id")
    Optional<Place> findByIdWithCategoriesAndTags(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Place p SET p.viewCount = p.viewCount + 1 WHERE p.id = :placeId")
    void incrementViewCount(@Param("placeId") Long placeId);

    @EntityGraph(attributePaths = {"placeCategories.category", "placeTags.tag"})
    @Query("SELECT DISTINCT p FROM Place p " +
            "JOIN p.placeCategories pc " +
            "WHERE pc.category.id = :categoryId " +
            "AND p.campus = :campus " +
            "AND p.status = 'APPROVED' " +
            "ORDER BY p.id DESC")
    List<Place> findByCategoryIdAndCampus(@Param("categoryId") Long categoryId, @Param("campus") Campus campus);

    @Query("SELECT p FROM Place p WHERE p.campus = :campus AND p.status = 'APPROVED' ORDER BY p.likeCount DESC")
    List<Place> findTopByCampusOrderByLikeCount(@Param("campus") Campus campus, Pageable pageable);

    @EntityGraph(attributePaths = {"placeCategories.category", "placeTags.tag"})
    @Query("SELECT p FROM Place p " +
            "WHERE p.name LIKE CONCAT('%', :keyword, '%') " +
            "AND p.status = 'APPROVED' " +
            "AND p.campus = :campus " +
            "ORDER BY p.id DESC")
    List<Place> searchByNameContainingAndCampus(@Param("keyword") String keyword, @Param("campus") Campus campus);

    @EntityGraph(attributePaths = {"placeCategories", "placeCategories.category", "placeTags", "placeTags.tag"})
    List<Place> findAllByRegisteredByOrderByCreatedAtDesc(User registeredBy);
}
