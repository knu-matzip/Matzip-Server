package com.matzip.place.infra.repository;

import com.matzip.place.domain.entity.Place;
import com.matzip.place.domain.entity.PlaceTag;
import com.matzip.place.domain.entity.PlaceTagId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaceTagRepository extends JpaRepository<PlaceTag, PlaceTagId> {
    List<PlaceTag> findAllByPlace(Place place);

    List<PlaceTag> findAllByPlaceIn(List<Place> places);
}
