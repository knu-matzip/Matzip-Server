package com.matzip.admin.repository;

import com.matzip.admin.domain.RequestReview;
import com.matzip.admin.domain.RequestReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RequestReviewRepository extends JpaRepository<RequestReview, Long> {

    Optional<RequestReview> findTopByPlaceIdAndStatusOrderByCreatedAtDesc(Long placeId, RequestReviewStatus status);
}
