package com.matzip.place.infra.repository;

import com.matzip.place.domain.Campus;
import com.matzip.place.domain.entity.Menu;
import com.matzip.place.domain.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findByPlaceOrderByIsRecommendedDescNameAsc(Place place);

    @Query("SELECT m FROM Menu m JOIN FETCH m.place p " +
            "WHERE m.name LIKE CONCAT('%', :keyword, '%') AND p.status = 'APPROVED' AND p.campus = :campus " +
            "ORDER BY p.id, m.name")
    List<Menu> findByNameContainingAndPlaceApprovedAndCampus(@Param("keyword") String keyword, @Param("campus") Campus campus);
}
