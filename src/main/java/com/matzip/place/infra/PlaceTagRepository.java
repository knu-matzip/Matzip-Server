package com.matzip.place.infra;

import com.matzip.place.domain.PlaceTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceTagRepository extends JpaRepository<PlaceTag, Long> {
}
