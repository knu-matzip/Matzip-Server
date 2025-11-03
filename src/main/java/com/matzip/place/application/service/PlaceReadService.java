package com.matzip.place.application.service;

import com.matzip.common.exception.BusinessException;
import com.matzip.common.exception.code.ErrorCode;
import com.matzip.place.api.request.MapSearchRequestDto;
import com.matzip.place.api.response.*;
import com.matzip.place.domain.entity.*;
import com.matzip.place.domain.*;
import com.matzip.place.infra.repository.*;
import com.matzip.place.dto.CategoryDto;
import com.matzip.place.dto.TagDto;
import com.matzip.user.infra.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceReadService {

    private final PlaceRepository placeRepository;
    private final MenuRepository menuRepository;
    private final PhotoRepository photoRepository;
    private final PlaceCategoryRepository placeCategoryRepository;
    private final PlaceTagRepository placeTagRepository;
    private final DailyViewCountRepository dailyViewCountRepository;
    private final CategoryRepository categoryRepository;
    private final PlaceLikeRepository placeLikeRepository;
    private final UserRepository userRepository;
    private final ViewCountService viewCountService;

    private static final int RANKING_SIZE = 10;

    @Transactional
    public PlaceDetailResponseDto getPlaceDetail(Long placeId, Long userId) {

        viewCountService.incrementAllCounts(placeId);

        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_FOUND));

        if (!place.isApproved()) {
            throw new BusinessException(ErrorCode.PLACE_NOT_FOUND);
        }

        PlaceRelatedData relatedData = getPlaceRelatedData(place);
        List<Menu> menus = menuRepository.findByPlaceOrderByIsRecommendedDescNameAsc(place);

        boolean isLiked = checkIfUserLikedPlace(userId, place);

        return PlaceDetailResponseDto.from(place, relatedData.photos(), menus, relatedData.categories(), relatedData.tags(), isLiked);
    }

    public List<MapSearchResponseDto> findPlacesInMapBounds(MapSearchRequestDto requestDto) {
        List<Place> places;

        // 사용자 위치 정보가 있으면 거리순 정렬 쿼리 호출
        if (requestDto.getUserLat() != null && requestDto.getUserLng() != null) {
            places = placeRepository.findWithinBoundsAndSortByDistance(
                    requestDto.getMinLat(),
                    requestDto.getMaxLat(),
                    requestDto.getMinLng(),
                    requestDto.getMaxLng(),
                    requestDto.getUserLat(),
                    requestDto.getUserLng()
            );
        } else {
            // 사용자 위치 정보가 없으면 기존 쿼리 호출
            places = placeRepository.findWithinBounds(
                    requestDto.getMinLat(),
                    requestDto.getMaxLat(),
                    requestDto.getMinLng(),
                    requestDto.getMaxLng()
            );
        }

        return places.stream()
                .map(place -> {
                    PlaceRelatedData relatedData = getPlaceRelatedData(place);
                    return MapSearchResponseDto.from(place, relatedData.photos(), relatedData.categories(), relatedData.tags());
                })
                .collect(Collectors.toList());
    }

    public List<PlaceCommonResponseDto> getRanking(Campus campus, String sort) {
        if ("views".equals(sort)) {
            return getDailyRankingByViews(campus);
        }

        // TODO: 찜 많은 맛집 기능 구현 후 추가하기
        return getDailyRankingByViews(campus);
    }

    private List<PlaceCommonResponseDto> getDailyRankingByViews(Campus campus) {
        LocalDate today = LocalDate.now();
        Pageable topN = PageRequest.of(0, RANKING_SIZE);

        List<DailyViewCount> dailyRankings = dailyViewCountRepository.findDailyRankingByCampus(campus, today, topN);

        return dailyRankings.stream()
                .map(dailyViewCount -> {
                    Place place = dailyViewCount.getPlace();
                    PlaceRelatedData relatedData = getPlaceRelatedData(place);
                    return PlaceCommonResponseDto.from(place, relatedData.categories(), relatedData.tags());
                })
                .collect(Collectors.toList());
    }

    /**
     * Place의 연관 데이터를 조회하는 공통 메서드
     */
    private PlaceRelatedData getPlaceRelatedData(Place place) {
        List<Photo> photos = photoRepository.findByPlaceOrderByDisplayOrderAsc(place);

        List<PlaceCategory> placeCategories = placeCategoryRepository.findAllByPlace(place);
        List<Category> categories = placeCategories.stream()
                .map(PlaceCategory::getCategory)
                .collect(Collectors.toList());

        List<PlaceTag> placeTags = placeTagRepository.findAllByPlace(place);
        List<Tag> tags = placeTags.stream()
                .map(PlaceTag::getTag)
                .collect(Collectors.toList());

        return new PlaceRelatedData(photos, categories, tags);
    }

    private boolean checkIfUserLikedPlace(Long userId, Place place) {
        if (userId == null) {
            return false;
        }

        return userRepository.findById(userId)
                .map(user -> placeLikeRepository.existsByUserAndPlace(user, place))
                .orElse(false);
    }

    public List<PlaceCommonResponseDto> getPlacesByCategory(Long categoryId, Campus campus) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        List<Place> places = placeRepository.findByCategoryIdAndCampus(categoryId, campus);

        return places.stream()
                .map(place -> {
                    List<Category> categories = place.getCategories();
                    List<Tag> tags = place.getTags();

                    List<CategoryDto> categoryDtos = categories.stream().map(CategoryDto::from).collect(Collectors.toList());
                    List<TagDto> tagDtos = tags.stream().map(TagDto::from).collect(Collectors.toList());

                    return PlaceCommonResponseDto.of(
                            place.getId(),
                            place.getName(),
                            place.getAddress(),
                            categoryDtos,
                            tagDtos
                    );
                })
                .collect(Collectors.toList());
    }

    private record PlaceRelatedData(List<Photo> photos,
                                    List<Category> categories,
                                    List<Tag> tags) {
    }
}
