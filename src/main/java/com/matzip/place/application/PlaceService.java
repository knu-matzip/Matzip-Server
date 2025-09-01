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
import java.util.*;
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
     */
    public PlaceCheckResponseDto preview(PlaceCheckRequestDto req) {
        final Long kakaoPlaceId = req.getKakaoPlaceId();

        // 1) 이미 등록 여부 조회 (예외 X, 불리언으로 응답)
        boolean already = placeRepository.existsByKakaoPlaceId(kakaoPlaceId);

        // 2) 카카오(패널)에서 메뉴/사진 수집
        KakaoApiClient.MenusAndPhotos mp = kakaoApiClient.getMenusAndPhotos(kakaoPlaceId);

        // 3) 메뉴를 프리뷰 전용 DTO(MenuItem)로 변환 (isRecommended=false)
        List<PlaceCheckResponseDto.MenuItem> menuItems = new ArrayList<>();
        List<MenuDto> srcMenus = mp.menus();
        for (MenuDto m : srcMenus) {
            PlaceCheckResponseDto.MenuItem item = PlaceCheckResponseDto.MenuItem.builder()
                    .name(m.getName())
                    .price(m.getPrice())
                    .isRecommended(false) // 프리뷰 단계: 사용자 선택 전이므로 항상 false
                    .build();
            menuItems.add(item);
        }

        // 4) 사진은 공용 PhotoDto를 그대로 전달
        List<PhotoDto> photos = mp.photos();

        // 5) 평탄화된 DTO로 응답 조립
        return PlaceCheckResponseDto.builder()
                .alreadyRegistered(already)
//                .placeName(req.getName())        // TODO 서버 신뢰 소스(공식 검색/스냅샷)로 대체
//                .address(req.getAddress())       // TODO 서버 신뢰 소스로 대체
//                .location(req.getLocation())     // TODO 서버 신뢰 소스로 대체
                .photos(photos)
                .menus(menuItems)
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
