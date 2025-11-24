package com.matzip.place.application.service;

import com.matzip.common.exception.BusinessException;
import com.matzip.common.exception.code.ErrorCode;
import com.matzip.place.api.response.PlaceLikeResponseDto;
import com.matzip.place.api.response.PlaceCommonResponseDto;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    public PlaceLikeResponseDto addLike(Long userId, Long placeId) {
        User user = findUserById(userId);
        Place place = findPlaceById(placeId);

        if (placeLikeRepository.existsByUserAndPlace(user, place)) {
            throw new BusinessException(ErrorCode.ALREADY_LIKED_PLACE);
        }

        placeLikeRepository.save(PlaceLike.of(user, place));
        place.incrementLikeCount();
        
        return PlaceLikeResponseDto.addLike(placeId);
    }

    @Transactional
    public PlaceLikeResponseDto removeLike(Long userId, Long placeId) {
        User user = findUserById(userId);
        Place place = findPlaceById(placeId);

        placeLikeRepository.findByUserAndPlace(user, place).ifPresent(placeLike -> {
            placeLikeRepository.delete(placeLike);
            place.decrementLikeCount();
        });
        
        return PlaceLikeResponseDto.removeLike(placeId);
    }

    @Transactional(readOnly = true)
    public List<PlaceCommonResponseDto> getLikedPlaces(Long userId) {
        User user = findUserById(userId);

        List<PlaceLike> likes = placeLikeRepository.findAllByUserWithPlace(user);
        List<Place> places = likes.stream().map(PlaceLike::getPlace).collect(Collectors.toList());

        if (places.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, List<Category>> placeIdToCategories = placeCategoryRepository.findAllByPlaceIn(places).stream()
                .collect(Collectors.groupingBy(
                        pc -> pc.getPlace().getId(),
                        Collectors.mapping(PlaceCategory::getCategory, Collectors.toList())
                ));

        Map<Long, List<Tag>> placeIdToTags = placeTagRepository.findAllByPlaceIn(places).stream()
                .collect(Collectors.groupingBy(
                        pt -> pt.getPlace().getId(),
                        Collectors.mapping(PlaceTag::getTag, Collectors.toList())
                ));

        return places.stream()
                .map(place -> {
                    List<Category> categories = placeIdToCategories.getOrDefault(place.getId(), Collections.emptyList());
                    List<Tag> tags = placeIdToTags.getOrDefault(place.getId(), Collections.emptyList());
                    return PlaceCommonResponseDto.from(place, categories, tags);
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
