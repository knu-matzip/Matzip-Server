package com.matzip.user.infra;

import com.matzip.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByKakaoId(Long kakaoId);

    Optional<User> findByKakaoId(Long kakaoId);
}
