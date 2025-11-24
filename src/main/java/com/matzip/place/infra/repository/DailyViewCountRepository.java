package com.matzip.place.infra.repository;

import com.matzip.place.domain.Campus;
import com.matzip.place.domain.DailyViewCount;
import com.matzip.place.domain.entity.Place;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyViewCountRepository extends JpaRepository<DailyViewCount, Long> {

    Optional<DailyViewCount> findByPlaceAndViewDate(Place place, LocalDate date);

    @Query("SELECT d FROM DailyViewCount d JOIN d.place p WHERE p.campus = :campus AND d.viewDate = :date ORDER BY d.count DESC")
    List<DailyViewCount> findDailyRankingByCampus(
            @Param("campus") Campus campus,
            @Param("date") LocalDate date,
            Pageable pageable
    );


    @Modifying
    @Query("UPDATE DailyViewCount d SET d.count = d.count + 1 WHERE d.place.id = :placeId AND d.viewDate = :date")
    int incrementDailyViewCount(@Param("placeId") Long placeId, @Param("date") LocalDate date);
}
