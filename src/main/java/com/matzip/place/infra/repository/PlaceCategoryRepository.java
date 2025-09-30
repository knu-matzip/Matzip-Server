package com.matzip.place.infra.repository;

import com.matzip.place.domain.entity.Place;
import com.matzip.place.domain.entity.PlaceCategory;
import com.matzip.place.domain.entity.PlaceCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaceCategoryRepository extends JpaRepository<PlaceCategory, PlaceCategoryId> {
    List<PlaceCategory> findAllByPlace(Place place);
}
