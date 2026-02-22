package com.matzip.place.application;

import com.matzip.place.api.request.PlaceRequestDto;
import com.matzip.place.application.port.PlaceTempStore;
import com.matzip.place.application.service.PlaceService;
import com.matzip.place.domain.Campus;
import com.matzip.place.domain.PlaceStatus;
import com.matzip.place.domain.entity.Category;
import com.matzip.place.domain.entity.PlaceCategory;
import com.matzip.place.infra.kakao.KakaoApiClient;
import com.matzip.place.infra.repository.*;
import com.matzip.user.infra.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.matzip.place.application.port.PlaceTempStore.PlaceSnapshot.SMenu;
import static com.matzip.place.application.port.PlaceTempStore.PlaceSnapshot.SPhoto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaceServiceTest {

    private static final String TEST_KAKAO_PLACE_ID = "1852074823";

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

    @Test
    @DisplayName("카테고리는 요청 categoryIds 순서대로 저장된다")
    void register_ShouldPersistCategoriesInRequestOrder() {
        PlaceRequestDto request = createPlaceRequest();
        request.setCategoryIds(List.of(2L, 1L));

        Category category1 = createMockCategory(1L);
        Category category2 = createMockCategory(2L);

        when(placeRepository.findByKakaoPlaceIdAndStatus(eq(TEST_KAKAO_PLACE_ID), eq(PlaceStatus.APPROVED)))
                .thenReturn(Optional.empty());
        when(placeTempStore.findById(TEST_KAKAO_PLACE_ID)).thenReturn(createMockPlaceSnapshot());
        when(categoryRepository.findAllById(List.of(2L, 1L))).thenReturn(List.of(category1, category2));

        placeService.register(request);

        ArgumentCaptor<PlaceCategory> captor = ArgumentCaptor.forClass(PlaceCategory.class);
        verify(placeCategoryRepository, times(2)).save(captor.capture());

        assertThat(captor.getAllValues())
                .extracting(pc -> pc.getCategory().getId())
                .containsExactly(2L, 1L);
        assertThat(captor.getAllValues())
                .extracting(PlaceCategory::getDisplayOrder)
                .containsExactly(0, 1);
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
