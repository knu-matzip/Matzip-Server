package com.matzip.place.application;

import com.matzip.common.dto.LocationDto;
import com.matzip.common.dto.MenuDto;
import com.matzip.common.dto.PhotoDto;
import com.matzip.place.infra.kakao.KakaoApiClient;
import com.matzip.place.api.request.PlaceCheckRequestDto;
import com.matzip.place.api.request.PlaceRequestDto;
import com.matzip.place.api.response.PlaceCheckResponseDto;
import com.matzip.place.api.response.PlaceRegisterResponseDto;
import com.matzip.place.application.port.PlaceTempStore.PlaceSnapshot;
import com.matzip.place.domain.*;
import com.matzip.place.application.port.PlaceTempStore;
import com.matzip.place.infra.repository.*;
import com.matzip.user.domain.User;
import com.matzip.user.infra.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.matzip.place.infra.kakao.KakaoApiClient.*;
import static com.matzip.place.api.request.PlaceRequestDto.*;
import static com.matzip.place.api.response.PlaceCheckResponseDto.*;
import static com.matzip.place.application.port.PlaceTempStore.PlaceSnapshot.*;

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
    private final UserRepository userRepository;
    private final PlaceTempStore placeTempStore;

    /**
     * 가게 정보 확인 프리뷰
     */
    public PlaceCheckResponseDto preview(PlaceCheckRequestDto req) {
        final String kakaoPlaceId = req.getKakaoPlaceId();

        // 이미 등록 여부 조회
        boolean already = placeRepository.existsByKakaoPlaceId(kakaoPlaceId);
        if (already) {
            // 이미 등록된 경우: 패널 호출 없이 최소 정보만 응답 후 반환
            // 주소는 빈 문자열("")
            // 사진/메뉴는 빈 배열
            // placeId, placeName, location 은 DB 기준으로 채움
            Optional<Place> maybe = placeRepository.findByKakaoPlaceId(kakaoPlaceId);
            if (maybe.isEmpty()) {
                // 경합 상황 방어(존재 체크 직후 삭제 등)
                throw new IllegalStateException("등록된 맛집 조회에 실패했습니다. kakaoPlaceId=" + kakaoPlaceId);
            }
            Place p = maybe.get();

            return builder()
                    .alreadyRegistered(true)
                    .placeName(p.getName())
                    .address("")
                    .location(LocationDto.of(p.getLatitude(), p.getLongitude()))
                    .photos(Collections.emptyList())
                    .menus(Collections.emptyList())
                    .build();
        }

        // 미등록이면 panel3에서 정보 가져오기
        final String kakaoPlaceIdStr = String.valueOf(kakaoPlaceId);
        PanelSnapshot snap = kakaoApiClient.getPanelSnapshot(kakaoPlaceIdStr);

        // 캐시에 스냅샷 저장 (등록 시 재사용)
        PlaceSnapshot cachedSnapshot = createPlaceSnapshot(snap);
        placeTempStore.put(cachedSnapshot);

        // 메뉴를 프리뷰 전용 DTO로 변환 (isRecommended=false 고정)
        List<MenuItem> menuItems = new ArrayList<>();
        List<MenuDto> srcMenus = snap.menus();
        if (srcMenus != null) {
            for (MenuDto m : srcMenus) {
                MenuItem item = MenuItem.builder()
                        .name(m.getName())
                        .price(m.getPrice())
                        .isRecommended(false) // 프리뷰 단계이므로 항상 false
                        .build();
                menuItems.add(item);
            }
        }

        return builder()
                .alreadyRegistered(false)
                .placeName(snap.placeName())
                .address(snap.address())
                .location(LocationDto.of(snap.latitude(), snap.longitude()))
                .photos(snap.photos())
                .menus(snap.menus())
                .build();
    }

    /**
     * 등록
     */
    @Transactional
    public PlaceRegisterResponseDto register(PlaceRequestDto req) {
        final String kakaoPlaceId = req.getKakaoPlaceId();

        // 1) 멱등 처리(이미 등록 요청이 있는 경우 기존 데이터 반환)
        Optional<Place> maybe = placeRepository.findByKakaoPlaceId(kakaoPlaceId);
        if (maybe.isPresent()) {
            Place exists = maybe.get();
            List<Category> existsCategories = extractCategoriesByPlace(exists);
            List<Tag> existsTags = extractTagsByPlace(exists);
            return PlaceRegisterResponseDto.from(exists, existsCategories, existsTags);
        }

        // 2) 캐시에서 스냅샷 조회 (프리뷰 단계에서 저장된 데이터 사용)
        PlaceSnapshot cachedSnapshot = placeTempStore.findById(kakaoPlaceId);

        // 3) 등록자 정보 조회 (nullable)
        User registeredBy = null;
        if (req.getRegisteredBy() != null) {
            registeredBy = userRepository.findById(req.getRegisteredBy())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. userId=" + req.getRegisteredBy()));
        }

        // 4) Place 저장 (승인 대기 상태로 저장)
        Place place = Place.builder()
                .campus(req.getCampus())
                .kakaoPlaceId(kakaoPlaceId)
                .name(cachedSnapshot.getPlaceName())
                .address(cachedSnapshot.getAddress())
                .latitude(cachedSnapshot.getLatitude())
                .longitude(cachedSnapshot.getLongitude())
                .description(req.getDescription())
                .registeredBy(registeredBy)
                .status(PlaceStatus.PENDING)
                .build();
        placeRepository.save(place);

        // 5) 카테고리 저장
        List<Long> categoryIds = req.getCategoryIds();
        List<Category> categories = categoryRepository.findAllById(categoryIds);
        if (categories.size() != categoryIds.size()) {
            throw new IllegalArgumentException("유효하지 않은 categoryId가 포함되어 있습니다.");
        }
        for (Category c : categories) {
            placeCategoryRepository.save(new PlaceCategory(place, c));
        }

        // 6) 태그 저장
        List<Tag> tags = new ArrayList<Tag>();
        List<Long> tagIds = req.getTagIds();
        if (tagIds != null && !tagIds.isEmpty()) {
            List<Tag> found = tagRepository.findAllById(tagIds);
            if (found.size() != tagIds.size()) {
                throw new IllegalArgumentException("유효하지 않은 tagId가 포함되어 있습니다.");
            }
            for (Tag t : found) {
                placeTagRepository.save(new PlaceTag(place, t));
                tags.add(t);
            }
        }

        // 7) 사진 저장
        LocalDateTime now = LocalDateTime.now();
        List<SPhoto> photos = cachedSnapshot.getPhotos();
        if (photos != null) {
            for (SPhoto sp : photos) {
                int order = sp.getDisplayOrder();
                photoRepository.save(
                        Photo.builder()
                                .place(place)
                                .photoUrl(sp.getPhotoUrl())
                                .displayOrder(order)
                                .fetchedAt(now)
                                .build()
                );
            }
        }

        // 8) 메뉴 저장
        // 요청으로 들어온 메뉴에서 "추천 여부"만 사용하기 위해 이름 -> 추천여부 매핑 생성
        Map<String, Boolean> recommendedByName = new HashMap<String, Boolean>();
        List<MenuInfo> reqMenus = req.getMenus();
        if (reqMenus != null) {
            for (MenuInfo mi : reqMenus) {
                if (mi != null && mi.getName() != null) {
                    // 마지막 값 우선(중복 이름 방어)
                    recommendedByName.put(mi.getName(), Boolean.TRUE.equals(mi.getIsRecommended()));
                }
            }
        }

        List<SMenu> menus = cachedSnapshot.getMenus();
        if (menus != null) {
            for (SMenu sm : menus) {
                String name = sm.getName();
                int price = sm.getPrice(); // 캐싱된 스냅샷에서 가격
                boolean isRec = false;
                if (name != null && recommendedByName.containsKey(name)) {
                    Boolean v = recommendedByName.get(name);
                    isRec = (v != null) && v.booleanValue();
                }

                menuRepository.save(
                        Menu.builder()
                                .place(place)
                                .name(name)
                                .price(price)
                                .isRecommended(isRec)
                                .build()
                );
            }
        }

        // 9) 캐시에서 스냅샷 제거
        placeTempStore.remove(kakaoPlaceId);

        // 10) 응답 조합
        return PlaceRegisterResponseDto.from(place, categories, tags);
    }

    // ===== 관리자 기능 (TODO: Admin 페이지 개발 시 구현) =====
    /**
     * TODO: 관리자가 Place 등록 요청을 승인하는 메서드
     * TODO: 관리자가 Place 등록 요청을 거부하는 메서드
     * TODO: 관리자가 승인 대기 중인 Place 목록을 조회하는 메서드
     */

    // ===== 내부 헬퍼 =====

    /**
     * KakaoApiClient.PanelSnapshot을 PlaceTempStore.PlaceSnapshot으로 변환
     */
    private PlaceSnapshot createPlaceSnapshot(PanelSnapshot snap) {
        // 메뉴 변환
        List<SMenu> sMenus = new ArrayList<>();
        if (snap.menus() != null) {
            for (MenuDto md : snap.menus()) {
                sMenus.add(new SMenu(md.getName(), md.getPrice()));
            }
        }

        // 사진 변환
        List<SPhoto> sPhotos = new ArrayList<>();
        if (snap.photos() != null) {
            for (PhotoDto pd : snap.photos()) {
                sPhotos.add(new SPhoto(
                    null, // photoId는 null로 설정
                    pd.getPhotoUrl(),
                    pd.getDisplayOrder() != null ? pd.getDisplayOrder() : 0
                ));
            }
        }

        return new PlaceSnapshot(
            snap.confirmId(), // confirmId를 사용
            snap.placeName(),
            snap.address(),
            snap.latitude(),
            snap.longitude(),
            sMenus,
            sPhotos
        );
    }

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
