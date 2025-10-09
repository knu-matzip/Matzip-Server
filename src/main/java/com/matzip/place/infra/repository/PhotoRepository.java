package com.matzip.place.infra.repository;

import com.matzip.place.domain.entity.Photo;
import com.matzip.place.domain.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
    List<Photo> findByPlaceOrderByDisplayOrderAsc(Place place);
}
