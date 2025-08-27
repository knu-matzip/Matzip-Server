package com.matzip.place.application;

import com.matzip.common.dto.MenuDto;
import com.matzip.common.dto.PhotoDto;
import com.matzip.common.exception.PlaceAlreadyExistsException;
import com.matzip.external.kakao.KakaoApiClient;
import com.matzip.place.api.request.PlaceCheckRequestDto;
import com.matzip.place.api.request.PlaceRequestDto;
import com.matzip.place.api.response.PlaceCheckResponseDto;
import com.matzip.place.api.response.PlaceRegisterResponseDto;
import com.matzip.place.domain.*;
import com.matzip.place.infra.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {

    private final KakaoApiClient kakaoApiClient;

    private final PlaceRepository placeRepository;
    private final PhotoRepository photoRepository;
    private final MenuRepository menuRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final PlaceCategoryRepository placeCategoryRepository;
    private final PlaceTagRepository placeTagRepository;

    /**
     * 가게 정보 확인 프리뷰
     * 프론트가 알고 있는 (name/address/location/campus)를 서버에 전달
     * 서버는 KakaoApiClient로 메뉴와 사진만 더하여 응답
     */
    public PlaceCheckResponseDto preview(PlaceCheckRequestDto req) {
        final Long kakaoPlaceId = req.getKakaoPlaceId();

        if (placeRepository.existsByKakaoPlaceId(kakaoPlaceId)) {
            throw new PlaceAlreadyExistsException("이미 등록된 맛집입니다. kakaoPlaceId=" + kakaoPlaceId);
        }

        // 2) Kakao에서 메뉴, 사진 수집
        KakaoApiClient.MenusAndPhotos mp = kakaoApiClient.getMenusAndPhotos(kakaoPlaceId);

        // 3) 프리뷰 응답 합치기(프론트가 전달한 정보 + Kakao)
        return PlaceCheckResponseDto.builder()
                .alreadyRegistered(false)
                .kakaoPlaceId(kakaoPlaceId)
                .campus(req.getCampus())
                .place(PlaceCheckResponseDto.PlaceInfo.builder()
                        .photos(mp.photos())
                        .build())
                .menus(mp.menus())
                .build();
    }

    /**
     * 등록
     */
    @Transactional
    public PlaceRegisterResponseDto register(PlaceRequestDto req) {
        final Long kakaoPlaceId = req.getKakaoPlaceId();

        // 1) 멱등 처리(이미 등록된 경우 기존 데이터 반환)
        Optional<Place> maybe = placeRepository.findByKakaoPlaceId(kakaoPlaceId);
        if (maybe.isPresent()) {
            Place exists = maybe.get();
            List<Category> existsCategories = extractCategoriesByPlace(exists);
            List<Tag> existsTags = extractTagsByPlace(exists);
            return PlaceRegisterResponseDto.from(exists, existsCategories, existsTags);
        }

        // 2) Place 저장
        Place place = Place.builder()
                .campus(req.getCampus())
                .kakaoPlaceId(kakaoPlaceId)
                .name(req.getName())
                .address(req.getAddress())
                .latitude(req.getLocation().getLatitude())
                .longitude(req.getLocation().getLongitude())
                .description(req.getDescription())
                .build();
        placeRepository.save(place);

        // 3) 카테고리, 태그 연관 저장
        List<Category> categories = categoryRepository.findAllById(req.getCategoryIds());
        if (categories.size() != req.getCategoryIds().size()) {
            throw new IllegalArgumentException("유효하지 않은 categoryId가 포함되어 있습니다.");
        }
        for (Category c : categories) {
            placeCategoryRepository.save(new PlaceCategory(place, c));
        }

        // 태그(선택) 여러 개
        List<Tag> tags = Collections.emptyList();
        if (req.getTagIds() != null && !req.getTagIds().isEmpty()) {
            tags = tagRepository.findAllById(req.getTagIds());
            // 누락 검증(선택사항)
            if (tags.size() != req.getTagIds().size()) {
                throw new IllegalArgumentException("유효하지 않은 tagId가 포함되어 있습니다.");
            }
            for (Tag t : tags) {
                placeTagRepository.save(new PlaceTag(place, t));
            }
        }

        // 4) Kakao에서 메뉴, 사진 수집 후 저장
        KakaoApiClient.MenusAndPhotos mp = kakaoApiClient.getMenusAndPhotos(kakaoPlaceId);

        // 4-1) 사진 url 저장
        LocalDateTime now = LocalDateTime.now();
        for (PhotoDto pd : mp.photos()) {
            photoRepository.save(
                    Photo.builder()
                            .place(place)
                            .photoUrl(pd.getPhotoUrl())
                            .displayOrder(pd.getDisplayOrder() != null ? pd.getDisplayOrder() : 0)
                            .fetchedAt(now)
                            .build()
            );
        }

        // 4-2) 메뉴 저장
        Map<String, PlaceRequestDto.MenuInfo> requestedMenuByName =
                req.getMenus().stream().collect(Collectors.toMap(PlaceRequestDto.MenuInfo::getName, m -> m, (a, b) -> a));

        for (MenuDto md : mp.menus()) {
            PlaceRequestDto.MenuInfo fromRequest = requestedMenuByName.get(md.getName());
            boolean isRec = fromRequest != null && Boolean.TRUE.equals(fromRequest.getIsRecommended());
            int price = md.getPrice(); // Kakao 파싱 가격(없으면 0)

            menuRepository.save(
                    Menu.builder()
                            .place(place)
                            .name(md.getName())
                            .price(price)
                            .isRecommended(isRec)
                            .build()
            );
        }

        // 5) 응답 조합
        return PlaceRegisterResponseDto.from(place, categories, tags);
    }

    // ===== 내부 헬퍼 =====

    // Place 기준으로 연결된 Category 목록을 조회
    private List<Category> extractCategoriesByPlace(Place place) {
        List<PlaceCategory> pcs = placeCategoryRepository.findAllByPlace(place);
        return pcs.stream().map(PlaceCategory::getCategory).collect(Collectors.toList());
    }

    // Place 기준으로 연결된 Tag 목록을 조회
    private List<Tag> extractTagsByPlace(Place place) {
        List<PlaceTag> pts = placeTagRepository.findAllByPlace(place);
        return pts.stream().map(PlaceTag::getTag).collect(Collectors.toList());
    }
}
