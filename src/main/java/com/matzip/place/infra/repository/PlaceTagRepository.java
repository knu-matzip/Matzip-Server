package com.matzip.place.infra.repository;

import com.matzip.place.domain.entity.Place;
import com.matzip.place.domain.entity.PlaceTag;
import com.matzip.place.domain.entity.PlaceTagId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlaceTagRepository extends JpaRepository<PlaceTag, PlaceTagId> {
    List<PlaceTag> findAllByPlace(Place place);

    @EntityGraph(attributePaths = {"tag"})
    @Query("SELECT pt FROM PlaceTag pt WHERE pt.place IN :places")
    List<PlaceTag> findAllByPlaceIn(@Param("places") List<Place> places);
}
