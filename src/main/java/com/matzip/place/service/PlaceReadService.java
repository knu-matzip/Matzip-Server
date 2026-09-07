package com.matzip.place.service;

import com.matzip.admin.domain.RequestReview;
import com.matzip.admin.domain.RequestReviewStatus;
import com.matzip.admin.repository.RequestReviewRepository;
import com.matzip.common.exception.BusinessException;
import com.matzip.common.exception.code.ErrorCode;
import com.matzip.place.dto.request.MapSearchRequestDto;
import com.matzip.place.dto.response.MapSearchResponseDto;
import com.matzip.place.dto.response.PlaceCommonResponseDto;
import com.matzip.place.dto.response.PlaceDetailResponseDto;
import com.matzip.place.dto.response.PlaceMenuSearchResponseDto;
import com.matzip.place.dto.response.PlaceRegisterStatusDetailResponseDto;
import com.matzip.place.dto.response.PlaceRegisterStatusResponseDto;
import com.matzip.place.dto.response.PlaceSearchResponseDto;
import com.matzip.place.domain.Campus;
import com.matzip.place.domain.DailyViewCount;
import com.matzip.place.domain.PlaceStatus;
import com.matzip.place.domain.SortType;
import com.matzip.place.domain.entity.Category;
import com.matzip.place.domain.entity.Menu;
import com.matzip.place.domain.entity.Photo;
import com.matzip.place.domain.entity.Place;
import com.matzip.place.domain.entity.PlaceCategory;
import com.matzip.place.domain.entity.PlaceTag;
import com.matzip.place.domain.entity.Tag;
import com.matzip.place.dto.CategoryDto;
import com.matzip.place.dto.TagDto;
import com.matzip.place.repository.CategoryRepository;
import com.matzip.place.repository.DailyViewCountRepository;
import com.matzip.place.repository.MenuRepository;
import com.matzip.place.repository.PhotoRepository;
import com.matzip.place.repository.PlaceCategoryRepository;
import com.matzip.place.repository.PlaceLikeRepository;
import com.matzip.place.repository.PlaceRepository;
import com.matzip.place.repository.PlaceTagRepository;
import com.matzip.user.domain.User;
import com.matzip.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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
    private final RequestReviewRepository requestReviewRepository;

    private static final int RANKING_SIZE = 3;
    private static final int LATEST_PLACES_SIZE = 5;

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

    public List<PlaceCommonResponseDto> getRanking(Campus campus, SortType sortType) {
        if (sortType == SortType.VIEWS) {
            return getDailyRankingByViews(campus);
        }

        if (sortType == SortType.LATEST) {
            return getRankingByLatest(campus);
        }

        return getRankingByLikes(campus);
    }

    /**
     * 오늘의 맛집(조회수 랭킹). 오늘 조회 데이터가 부족한 새벽/저트래픽 구간에 빈 값이 반환되지 않도록
     * 오늘(3곳 이상) → 어제(3곳 이상) → 전체 누적 조회수 순으로 폴백한다.
     */
    private List<PlaceCommonResponseDto> getDailyRankingByViews(Campus campus) {
        Pageable topN = PageRequest.of(0, RANKING_SIZE);
        LocalDate today = LocalDate.now();

        List<Place> todayRanking = findDailyRankingPlaces(campus, today, topN);
        if (todayRanking.size() >= RANKING_SIZE) {
            return buildRankingResponse(todayRanking);
        }

        List<Place> yesterdayRanking = findDailyRankingPlaces(campus, today.minusDays(1), topN);
        if (yesterdayRanking.size() >= RANKING_SIZE) {
            return buildRankingResponse(yesterdayRanking);
        }

        List<Place> viewCountRanking = placeRepository.findTopByCampusOrderByViewCount(campus, topN);
        return buildRankingResponse(viewCountRanking);
    }

    private List<Place> findDailyRankingPlaces(Campus campus, LocalDate date, Pageable pageable) {
        return dailyViewCountRepository.findDailyRankingByCampus(campus, date, pageable).stream()
                .map(DailyViewCount::getPlace)
                .collect(Collectors.toList());
    }

    private List<PlaceCommonResponseDto> getRankingByLikes(Campus campus) {
        Pageable topN = PageRequest.of(0, RANKING_SIZE);
        return buildRankingResponse(placeRepository.findTopByCampusOrderByLikeCount(campus, topN));
    }

    private List<PlaceCommonResponseDto> getRankingByLatest(Campus campus) {
        Pageable topN = PageRequest.of(0, LATEST_PLACES_SIZE);
        return buildRankingResponse(placeRepository.findTopByCampusOrderByCreatedAtDesc(campus, topN));
    }

    /**
     * Place 목록을 연관 데이터(카테고리/태그)와 함께 응답 DTO로 변환하는 공통 로직.
     */
    private List<PlaceCommonResponseDto> buildRankingResponse(List<Place> places) {
        Map<Long, PlaceRelatedData> relatedDataMap = getPlaceRelatedDataInBatch(places);

        return places.stream()
                .map(place -> {
                    PlaceRelatedData relatedData = relatedDataMap.get(place.getId());
                    return PlaceCommonResponseDto.from(place, relatedData.categories(), relatedData.tags());
                })
                .collect(Collectors.toList());
    }

    /**
     * Place의 연관 데이터를 조회하는 공통 메서드
     */
    private PlaceRelatedData getPlaceRelatedData(Place place) {
        List<Photo> photos = photoRepository.findByPlaceOrderByDisplayOrderAsc(place);

        List<PlaceCategory> placeCategories = placeCategoryRepository.findAllByPlaceOrderByDisplayOrderAsc(place);
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


    private Map<Long, PlaceRelatedData> getPlaceRelatedDataInBatch(List<Place> places) {
        if (places.isEmpty()) {
            return Map.of();
        }

        List<Photo> allPhotos = photoRepository.findByPlaceInOrderByDisplayOrderAsc(places);
        List<PlaceCategory> allPlaceCategories = placeCategoryRepository.findAllByPlaceIn(places);
        List<PlaceTag> allPlaceTags = placeTagRepository.findAllByPlaceIn(places);

        Map<Long, List<Photo>> photosByPlaceId = allPhotos.stream()
                .collect(Collectors.groupingBy(photo -> photo.getPlace().getId()));

        Map<Long, List<Category>> categoriesByPlaceId = allPlaceCategories.stream()
                .collect(Collectors.groupingBy(
                        pc -> pc.getPlace().getId(),
                        Collectors.mapping(PlaceCategory::getCategory, Collectors.toList())
                ));

        Map<Long, List<Tag>> tagsByPlaceId = allPlaceTags.stream()
                .collect(Collectors.groupingBy(
                        pt -> pt.getPlace().getId(),
                        Collectors.mapping(PlaceTag::getTag, Collectors.toList())
                ));

        return places.stream()
                .collect(Collectors.toMap(
                        Place::getId,
                        place -> new PlaceRelatedData(
                                photosByPlaceId.getOrDefault(place.getId(), List.of()),
                                categoriesByPlaceId.getOrDefault(place.getId(), List.of()),
                                tagsByPlaceId.getOrDefault(place.getId(), List.of())
                        )
                ));
    }

    public List<PlaceSearchResponseDto> searchPlaceDetails(String keyword, Campus campus) {
        List<Place> places = placeRepository.searchByNameContainingAndCampus(keyword, campus);

        return places.stream()
                .map(PlaceSearchResponseDto::from)
                .collect(Collectors.toList());
    }

    public List<PlaceMenuSearchResponseDto> searchPlaceByMenuName(String keyword, Campus campus) {
        List<Menu> menus = menuRepository.findByNameContainingAndPlaceApprovedAndCampus(keyword, campus);

        return menus.stream()
                .map(PlaceMenuSearchResponseDto::from)
                .collect(Collectors.toList());
    }

    public List<PlaceRegisterStatusResponseDto> getMyPlaceRequests(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<Place> myPlaces = placeRepository.findAllByRegisteredByOrderByCreatedAtDesc(user);

        return myPlaces.stream()
                .map(PlaceRegisterStatusResponseDto::from)
                .collect(Collectors.toList());
    }

    public PlaceRegisterStatusDetailResponseDto getMyPlaceRequestDetail(Long placeId, Long userId) {
        Place place = placeRepository.findByIdWithCategoriesAndTags(placeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_FOUND));

        if (place.getRegisteredBy() == null || !place.getRegisteredBy().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        List<Photo> photos = photoRepository.findByPlaceOrderByDisplayOrderAsc(place);
        List<Menu> menus = menuRepository.findByPlaceOrderByIsRecommendedDescNameAsc(place);
        List<Category> categories = place.getCategories();
        List<Tag> tags = place.getTags();
        String rejectedReason = resolveRejectedReason(place);

        return PlaceRegisterStatusDetailResponseDto.from(place, photos, menus, categories, tags, rejectedReason);
    }

    private String resolveRejectedReason(Place place) {
        if (place.getStatus() != PlaceStatus.REJECTED) {
            return null;
        }

        return requestReviewRepository.findTopByPlaceIdAndStatusOrderByCreatedAtDesc(
                        place.getId(),
                        RequestReviewStatus.REJECTED
                )
                .map(RequestReview::getRejectedReason)
                .orElse(null);
    }


    private record PlaceRelatedData(List<Photo> photos,
                                    List<Category> categories,
                                    List<Tag> tags) {
    }
}
