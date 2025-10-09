package com.matzip.place.infra.repository;

import com.matzip.place.domain.entity.Photo;
import com.matzip.place.domain.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
    List<Photo> findByPlaceOrderByDisplayOrderAsc(Place place);

    @Query("SELECT p FROM Photo p WHERE p.place IN :places ORDER BY p.displayOrder ASC")
    List<Photo> findByPlaceInOrderByDisplayOrderAsc(@Param("places") List<Place> places);
}
