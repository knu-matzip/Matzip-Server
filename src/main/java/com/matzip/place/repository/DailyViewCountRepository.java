package com.matzip.place.repository;

import com.matzip.place.domain.Campus;
import com.matzip.place.domain.DailyViewCount;
import com.matzip.place.domain.entity.Place;
import com.matzip.place.dto.PlaceViewTallyDto;
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

    @Query("SELECT d FROM DailyViewCount d JOIN FETCH d.place p WHERE p.campus = :campus AND d.viewDate = :date AND p.status = 'APPROVED' ORDER BY d.count DESC")
    List<DailyViewCount> findDailyRankingByCampus(
            @Param("campus") Campus campus,
            @Param("date") LocalDate date,
            Pageable pageable
    );


    @Modifying
    @Query("UPDATE DailyViewCount d SET d.count = d.count + 1 WHERE d.place.id = :placeId AND d.viewDate = :date")
    int incrementDailyViewCount(@Param("placeId") Long placeId, @Param("date") LocalDate date);

    // 동시 첫 조회로 같은 날짜 행이 중복될 수 있어 place별로 합산한다
    @Query("SELECT new com.matzip.place.dto.PlaceViewTallyDto(d.place.id, SUM(d.count)) " +
            "FROM DailyViewCount d WHERE d.viewDate = :date GROUP BY d.place.id")
    List<PlaceViewTallyDto> sumByPlaceOn(@Param("date") LocalDate date);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM DailyViewCount d WHERE d.viewDate = :date")
    int deleteByViewDate(@Param("date") LocalDate date);
}
