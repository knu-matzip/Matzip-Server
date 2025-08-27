package com.matzip.place.infra;

import com.matzip.place.domain.Place;
import com.matzip.place.domain.PlaceCategory;
import com.matzip.place.domain.PlaceCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaceCategoryRepository extends JpaRepository<PlaceCategory, PlaceCategoryId> {
    List<PlaceCategory> findAllByPlace(Place place);
}
