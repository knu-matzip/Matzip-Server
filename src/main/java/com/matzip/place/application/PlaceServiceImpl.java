package com.matzip.place.application;

import com.matzip.common.exception.BusinessException;
import com.matzip.common.exception.ExceptionUtils;
import com.matzip.common.exception.code.ErrorCode;
import com.matzip.place.api.request.PlaceRequestDto;
import com.matzip.place.api.response.PlaceCheckResponseDto;
import com.matzip.place.domain.*;
import com.matzip.place.infra.*;
import com.matzip.user.domain.User;
import com.matzip.user.infra.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceServiceImpl implements PlaceService {

    private final PlaceRepository placeRepository;
    private final UserRepository userRepository;
    private final MenuRepository menuRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final PlaceCategoryRepository placeCategoryRepository;
    private final PlaceTagRepository placeTagRepository;
//    private final KakaoApiClient kakaoApiClient; // 카카오맵 API 호출을 위한 클라이언트

    /**
     * 프리뷰 확인 로직
     */
    @Override
    public PlaceCheckResponseDto checkPlace(Long kakaoPlaceId) {
        // 1. 우리 DB에서 중복 여부 확인
        boolean alreadyRegistered = placeRepository.existsByKakaoPlaceId(kakaoPlaceId);

        // Todo 카카오맵 API를 호출해서 정보 받아오기, DTO로 변환하여 반환
    }

    /**
     * 최종 등록 로직
     */
    @Override
    @Transactional
    public Long registerPlace(PlaceRequestDto requestDto, Optional<Long> userId) {

        // 중복 등록 최종 확인
        ExceptionUtils.throwIf(
                placeRepository.existsByKakaoPlaceId(requestDto.getKakaoPlaceId()),
                ErrorCode.PLACE_ALREADY_EXISTS
        );

        // 등록자(User) 정보 조회 (비로그인 시 null)
        User creator = userId.map(id -> userRepository.findById(id)
                        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND)))
                .orElse(null);

        // Todo Place 엔티티 생성 및 Menu, PlaceCategory, PlaceTag 저장


    }

    // -- private helper methods --

    private void saveMenus(Place place, List<PlaceRequestDto.MenuInfo> menuInfos) {
        if (menuInfos == null || menuInfos.isEmpty()) return;
        List<Menu> menus = menuInfos.stream()
                .map(menuInfo -> Menu.builder()
                        .place(place)
                        .name(menuInfo.getName())
                        .price(menuInfo.getPrice())
                        .isRecommended(menuInfo.getIsRecommended())
                        .build())
                .collect(Collectors.toList());
        menuRepository.saveAll(menus);
    }

    private void savePlaceCategories(Place place, List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) return;
        List<Category> categories = categoryRepository.findAllById(categoryIds);
        List<PlaceCategory> placeCategories = categories.stream()
                .map(category -> new PlaceCategory(place, category))
                .collect(Collectors.toList());
        placeCategoryRepository.saveAll(placeCategories);
    }

    private void savePlaceTags(Place place, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) return;
        List<Tag> tags = tagRepository.findAllById(tagIds);
        List<PlaceTag> placeTags = tags.stream()
                .map(tag -> new PlaceTag(place, tag))
                .collect(Collectors.toList());
        placeTagRepository.saveAll(placeTags);
    }
}
