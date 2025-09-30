package com.matzip.place.application.service;

import com.matzip.common.exception.BusinessException;
import com.matzip.common.exception.code.ErrorCode;
import com.matzip.place.api.response.LikedPlaceResponseDto;
import com.matzip.place.domain.entity.*;
import com.matzip.place.infra.repository.PlaceCategoryRepository;
import com.matzip.place.infra.repository.PlaceLikeRepository;
import com.matzip.place.infra.repository.PlaceRepository;
import com.matzip.place.infra.repository.PlaceTagRepository;
import com.matzip.user.domain.User;
import com.matzip.user.infra.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlaceLikeService {

    private final PlaceLikeRepository placeLikeRepository;
    private final UserRepository userRepository;
    private final PlaceRepository placeRepository;
    private final PlaceCategoryRepository placeCategoryRepository; // Category 조회를 위해 추가
    private final PlaceTagRepository placeTagRepository;

    @Transactional
    public void addLike(Long userId, Long placeId) {
        User user = findUserById(userId);
        Place place = findPlaceById(placeId);

        placeLikeRepository.save(PlaceLike.of(user, place));
        place.incrementLikeCount();
    }

    @Transactional
    public void removeLike(Long userId, Long placeId) {
        User user = findUserById(userId);
        Place place = findPlaceById(placeId);

        PlaceLike placeLike = placeLikeRepository.findByUserAndPlace(user, place)
                .orElseThrow(() -> new BusinessException(ErrorCode.LIKE_NOT_FOUND));

        placeLikeRepository.delete(placeLike);
        place.decrementLikeCount();
    }

    @Transactional(readOnly = true)
    public List<LikedPlaceResponseDto> getLikedPlaces(Long userId) {
        User user = findUserById(userId);
        List<PlaceLike> likes = placeLikeRepository.findAllByUserOrderByCreatedAtDesc(user);

        return likes.stream()
                .map(like -> {
                    Place place = like.getPlace();

                    List<Category> categories = placeCategoryRepository.findAllByPlace(place).stream()
                            .map(PlaceCategory::getCategory)
                            .collect(Collectors.toList());

                    List<Tag> tags = placeTagRepository.findAllByPlace(place).stream()
                            .map(PlaceTag::getTag)
                            .collect(Collectors.toList());

                    return LikedPlaceResponseDto.from(place, categories, tags);
                })
                .collect(Collectors.toList());
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private Place findPlaceById(Long placeId) {
        return placeRepository.findById(placeId).orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_FOUND));
    }
}
