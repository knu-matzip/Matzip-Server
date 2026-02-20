package com.matzip.place.infra.repository;

import com.matzip.place.domain.entity.Place;
import com.matzip.place.domain.entity.PlaceCategory;
import com.matzip.place.domain.entity.PlaceCategoryId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlaceCategoryRepository extends JpaRepository<PlaceCategory, PlaceCategoryId> {
    List<PlaceCategory> findAllByPlaceOrderByDisplayOrderAsc(Place place);

    @EntityGraph(attributePaths = {"category"})
    @Query("SELECT pc FROM PlaceCategory pc WHERE pc.place IN :places ORDER BY pc.place.id ASC, pc.displayOrder ASC")
    List<PlaceCategory> findAllByPlaceIn(@Param("places") List<Place> places);
}
