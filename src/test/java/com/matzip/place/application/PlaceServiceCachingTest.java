package com.matzip.place.application;

import com.matzip.place.dto.MenuDto;
import com.matzip.place.dto.PhotoDto;
import com.matzip.place.application.service.PlaceService;
import com.matzip.place.infra.kakao.KakaoApiClient;
import com.matzip.place.api.request.PlaceCheckRequestDto;
import com.matzip.place.api.request.PlaceRequestDto;
import com.matzip.place.api.response.PlaceCheckResponseDto;
import com.matzip.place.application.port.PlaceTempStore;
import com.matzip.place.domain.entity.Category;
import com.matzip.place.domain.Campus;
import com.matzip.place.infra.repository.*;
import com.matzip.user.infra.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.matzip.place.application.port.PlaceTempStore.PlaceSnapshot.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaceServiceCachingTest {

    @InjectMocks
    private PlaceService placeService;

    @Mock private KakaoApiClient kakaoApiClient;
    @Mock private PlaceTempStore placeTempStore;
    @Mock private PlaceRepository placeRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private TagRepository tagRepository;
    @Mock private UserRepository userRepository;
    @Mock private PhotoRepository photoRepository;
    @Mock private MenuRepository menuRepository;
    @Mock private PlaceCategoryRepository placeCategoryRepository;
    @Mock private PlaceTagRepository placeTagRepository;


    private static final String TEST_KAKAO_PLACE_ID = "1852074823";

    @Test
    @DisplayName("프리뷰 단계에서 신규 장소의 정보가 캐싱된다")
    void preview_ShouldSaveSnapshotToCache() {
        // given
        PlaceCheckRequestDto request = createPlaceCheckRequest();
        KakaoApiClient.PanelSnapshot mockSnapshot = createMockPanelSnapshot();

        when(placeRepository.existsByKakaoPlaceIdAndStatus(anyString(), any())).thenReturn(false);
        when(kakaoApiClient.getPanelSnapshot(TEST_KAKAO_PLACE_ID)).thenReturn(mockSnapshot);

        // when
        PlaceCheckResponseDto result = placeService.preview(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getAlreadyRegistered()).isFalse();
        assertThat(result.getPlaceName()).isEqualTo("카페카키");
        assertThat(result.getAddress()).isEqualTo("충남 천안시 서북구 부성14길 46 지광빌딩 1층");

        // 캐시에 스냅샷이 저장되었는지 확인
        verify(placeTempStore, times(1)).put(any(PlaceTempStore.PlaceSnapshot.class));
    }

    @Test
    @DisplayName("등록 요청 단계에서 캐싱된 정보를 활용하고 외부 API 호출을 하지 않는다")
    void register_ShouldUseCacheAndReduceKakaoApiCalls() {
        // given
        PlaceRequestDto request = createPlaceRequest();
        PlaceTempStore.PlaceSnapshot cachedSnapshot = createMockPlaceSnapshot();

        Category category = createMockCategory(1L);
        when(placeRepository.findByKakaoPlaceId(TEST_KAKAO_PLACE_ID)).thenReturn(Optional.empty());
        when(placeTempStore.findById(TEST_KAKAO_PLACE_ID)).thenReturn(cachedSnapshot);
        when(categoryRepository.findAllById(List.of(1L))).thenReturn(List.of(category));

        // when
        placeService.register(request);

        // then
        // 카카오 API 호출이 없는지 확인 (캐시 사용)
        verify(kakaoApiClient, never()).getPanelSnapshot(anyString());

        // 캐시를 조회하고, 등록 완료 후 제거했는지 확인
        verify(placeTempStore, times(1)).findById(TEST_KAKAO_PLACE_ID);
        verify(placeTempStore, times(1)).remove(TEST_KAKAO_PLACE_ID);
    }

    private PlaceCheckRequestDto createPlaceCheckRequest() {
        PlaceCheckRequestDto request = new PlaceCheckRequestDto();
        request.setKakaoPlaceId(TEST_KAKAO_PLACE_ID);
        return request;
    }

    private PlaceRequestDto createPlaceRequest() {
        PlaceRequestDto request = new PlaceRequestDto();
        request.setKakaoPlaceId(TEST_KAKAO_PLACE_ID);
        request.setCampus(Campus.SINGWAN);
        request.setDescription("테스트 설명");
        request.setMenus(Collections.emptyList());
        request.setCategoryIds(List.of(1L));
        request.setTagIds(Collections.emptyList());
        return request;
    }

    private KakaoApiClient.PanelSnapshot createMockPanelSnapshot() {
        return new KakaoApiClient.PanelSnapshot(
                TEST_KAKAO_PLACE_ID,
                "카페카키",
                "충남 천안시 서북구 부성14길 46 지광빌딩 1층",
                37.123456,
                127.123456,
                List.of(MenuDto.builder().menuId(1L).name("테스트 메뉴").price(10000).build()),
                List.of(PhotoDto.builder().photoId(1L).photoUrl("http://test.com/photo.jpg").displayOrder(1).build())
        );
    }

    private PlaceTempStore.PlaceSnapshot createMockPlaceSnapshot() {
        return new PlaceTempStore.PlaceSnapshot(
                TEST_KAKAO_PLACE_ID,
                "카페카키",
                "충남 천안시 서북구 부성14길 46 지광빌딩 1층",
                37.123456,
                127.123456,
                List.of(new SMenu(1L, "테스트 메뉴", 10000)),
                List.of(new SPhoto(null, "http://test.com/photo.jpg", 1))
        );
    }

    private Category createMockCategory(Long id) {
        Category category = mock(Category.class);
        when(category.getId()).thenReturn(id);
        return category;
    }

}
