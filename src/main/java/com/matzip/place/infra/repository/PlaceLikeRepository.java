package com.matzip.place.infra.repository;

import com.matzip.place.domain.entity.Place;
import com.matzip.place.domain.entity.PlaceLike;
import com.matzip.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlaceLikeRepository extends JpaRepository<PlaceLike, Long> {

    // 찜 취소 시 특정 찜 내역을 찾기 위해 사용
    Optional<PlaceLike> findByUserAndPlace(User user, Place place);

    // 사용자의 찜 목록 조회를 위해 사용 (최신순 정렬)
    List<PlaceLike> findAllByUserOrderByCreatedAtDesc(User user);

    boolean existsByUserAndPlace(User user, Place place);
}
