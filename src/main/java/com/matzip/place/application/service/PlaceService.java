package com.matzip.place.application.service;

import com.matzip.common.exception.ValidationException;
import com.matzip.common.exception.code.ErrorCode;
import com.matzip.place.dto.LocationDto;
import com.matzip.place.dto.MenuDto;
import com.matzip.place.dto.PhotoDto;
import com.matzip.place.domain.entity.*;
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

    public PlaceCheckResponseDto preview(PlaceCheckRequestDto req) {
        final String kakaoPlaceId = req.getKakaoPlaceId();

        boolean already = placeRepository.existsByKakaoPlaceId(kakaoPlaceId);
        if (already) {

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

        final String kakaoPlaceIdStr = String.valueOf(kakaoPlaceId);
        PanelSnapshot snap = kakaoApiClient.getPanelSnapshot(kakaoPlaceIdStr);

        PlaceSnapshot cachedSnapshot = createPlaceSnapshot(snap);
        placeTempStore.put(cachedSnapshot);

        List<MenuItem> menuItems = new ArrayList<>();
        List<MenuDto> srcMenus = snap.menus();
        if (srcMenus != null) {
            for (MenuDto m : srcMenus) {
                MenuItem item = MenuItem.builder()
                        .menuId(m.getMenuId())
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
                .menus(menuItems)
                .build();
    }


    @Transactional
    public PlaceRegisterResponseDto register(PlaceRequestDto req) {
        final String kakaoPlaceId = req.getKakaoPlaceId();

        Optional<Place> maybe = placeRepository.findByKakaoPlaceId(kakaoPlaceId);
        if (maybe.isPresent()) {
            Place exists = maybe.get();
            List<Category> existsCategories = extractCategoriesByPlace(exists);
            List<Tag> existsTags = extractTagsByPlace(exists);
            return PlaceRegisterResponseDto.from(exists, existsCategories, existsTags);
        }

        PlaceSnapshot cachedSnapshot = placeTempStore.findById(kakaoPlaceId);
        if (cachedSnapshot == null) {
            throw new ValidationException(ErrorCode.VALIDATION_ERROR, "맛집 프리뷰 정보가 만료되었거나 존재하지 않습니다. 프리뷰를 다시 진행해주세요.");
        }

        User registeredBy = null;
        if (req.getRegisteredBy() != null) {
            registeredBy = userRepository.findById(req.getRegisteredBy())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. userId=" + req.getRegisteredBy()));
        }

        Place place = Place.builder()
                .campus(req.getCampus())
                .kakaoPlaceId(kakaoPlaceId)
                .name(cachedSnapshot.getPlaceName())
                .address(cachedSnapshot.getAddress())
                .latitude(cachedSnapshot.getLatitude())
                .longitude(cachedSnapshot.getLongitude())
                .description(req.getDescription())
                .registeredBy(registeredBy)
                .status(PlaceStatus.PENDING) // 승인 대기 상태로 저장
                .build();
        placeRepository.save(place);

        List<Long> categoryIds = req.getCategoryIds();
        List<Category> categories = categoryRepository.findAllById(categoryIds);
        if (categories.size() != categoryIds.size()) {
            throw new IllegalArgumentException("유효하지 않은 categoryId가 포함되어 있습니다.");
        }
        for (Category c : categories) {
            placeCategoryRepository.save(new PlaceCategory(place, c));
        }

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

        Map<Long, Boolean> recommendedById = new HashMap<>();
        Map<String, Boolean> recommendedByName = new HashMap<>();
        List<MenuInfo> reqMenus = req.getMenus();
        if (reqMenus != null) {
            for (MenuInfo mi : reqMenus) {
                if (mi != null) {
                    Boolean isRecommended = Boolean.TRUE.equals(mi.getIsRecommended());
                    if (mi.getMenuId() != null) {
                        recommendedById.put(mi.getMenuId(), isRecommended);
                    } else if (mi.getName() != null) {
                        // 마지막 값 우선(중복 이름 방어)
                        recommendedByName.put(mi.getName(), isRecommended);
                    }
                }
            }
        }

        List<SMenu> menus = cachedSnapshot.getMenus();
        if (menus != null) {
            for (SMenu sm : menus) {
                String name = sm.getName();
                int price = sm.getPrice();
                boolean isRec = false;
                if (sm.getMenuId() != null && recommendedById.containsKey(sm.getMenuId())) {
                    Boolean v = recommendedById.get(sm.getMenuId());
                    isRec = v != null && v.booleanValue();
                } else if (name != null && recommendedByName.containsKey(name)) {
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

        placeTempStore.remove(kakaoPlaceId);

        return PlaceRegisterResponseDto.from(place, categories, tags);
    }


    private PlaceSnapshot createPlaceSnapshot(PanelSnapshot snap) {
        List<SMenu> sMenus = new ArrayList<>();
        if (snap.menus() != null) {
            for (MenuDto md : snap.menus()) {
                sMenus.add(new SMenu(md.getMenuId(), md.getName(), md.getPrice()));
            }
        }

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
            snap.confirmId(),
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
