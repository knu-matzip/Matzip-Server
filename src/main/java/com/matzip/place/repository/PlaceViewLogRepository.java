package com.matzip.place.repository;

import com.matzip.place.domain.entity.PlaceViewLog;
import com.matzip.place.dto.PlaceViewTallyDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PlaceViewLogRepository extends JpaRepository<PlaceViewLog, Long> {

    @Query("SELECT new com.matzip.place.dto.PlaceViewTallyDto(l.placeId, COUNT(l)) " +
            "FROM PlaceViewLog l WHERE l.viewDate = :date GROUP BY l.placeId")
    List<PlaceViewTallyDto> countByPlaceOn(@Param("date") LocalDate date);

    @Modifying
    @Query("DELETE FROM PlaceViewLog l WHERE l.viewDate < :date")
    int deleteByViewDateBefore(@Param("date") LocalDate date);
}
