package com.matzip.place.infra;

import com.matzip.place.domain.PlaceCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceCategoryRepository extends JpaRepository<PlaceCategory, Long> {
}
