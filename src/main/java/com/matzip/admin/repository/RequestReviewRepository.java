package com.matzip.admin.repository;

import com.matzip.admin.domain.RequestReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestReviewRepository extends JpaRepository<RequestReview, Long> {
}
