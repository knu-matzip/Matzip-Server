package com.matzip.place.application.service;

import com.matzip.common.exception.BusinessException;
import com.matzip.common.exception.code.ErrorCode;
import com.matzip.place.api.request.MapSearchRequestDto;
import com.matzip.place.api.response.MapSearchResponseDto;
import com.matzip.place.api.response.PlaceDetailResponseDto;
import com.matzip.place.domain.entity.*;
import com.matzip.place.infra.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public PlaceDetailResponseDto getPlaceDetail(Long placeId, Long userId) {

        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_FOUND));

        // 승인된 맛집만 조회 가능
        if (!place.isApproved()) {
            throw new BusinessException(ErrorCode.PLACE_NOT_FOUND);
        }

        PlaceRelatedData relatedData = getPlaceRelatedData(place);
        List<Menu> menus = menuRepository.findByPlaceOrderByIsRecommendedDescNameAsc(place);

        // 좋아요 여부 확인 (현재는 임시로 false 반환)
        boolean isLiked = false; // TODO: 실제 좋아요 기능 구현 시 userId로 확인

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

    private record PlaceRelatedData(List<Photo> photos,
                                    List<Category> categories,
                                    List<Tag> tags) {
    }
}
