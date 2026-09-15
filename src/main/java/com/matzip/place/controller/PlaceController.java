package com.matzip.place.controller;

import com.matzip.common.response.ApiResponse;
import com.matzip.common.security.UserPrincipal;
import com.matzip.place.dto.request.PlaceCheckRequestDto;
import com.matzip.place.dto.request.PlaceRequestDto;
import com.matzip.place.dto.response.PlaceCheckResponseDto;
import com.matzip.place.dto.response.PlaceRegisterResponseDto;
import com.matzip.place.dto.response.PlaceMenuSearchResponseDto;
import com.matzip.place.dto.response.PlaceSearchResponseDto;
import com.matzip.place.service.PlaceReadService;
import com.matzip.place.service.PlaceService;
import com.matzip.place.domain.Campus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "맛집", description = "맛집 등록·검색 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/places")
public class PlaceController {

    private final PlaceService placeService;
    private final PlaceReadService placeReadService;

    // 프리뷰
    @Operation(summary = "맛집 등록 프리뷰", description = "카카오 장소 ID로 등록 전 미리보기 정보를 조회한다.")
    @GetMapping("/preview")
    public ApiResponse<PlaceCheckResponseDto> preview(@Valid @ModelAttribute PlaceCheckRequestDto req) {
        PlaceCheckResponseDto data = placeService.preview(req);
        return ApiResponse.success(data);
    }

    // 등록
    @Operation(summary = "맛집 등록", description = "맛집 등록을 요청한다. 로그인 시 등록자 정보가 함께 저장된다.")
    @PostMapping
    public ApiResponse<PlaceRegisterResponseDto> register(
            @Valid @RequestBody PlaceRequestDto req,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        if (userPrincipal != null) {
            req.setRegisteredBy(userPrincipal.getUserId());
        }

        PlaceRegisterResponseDto data = placeService.register(req);
        return ApiResponse.success(data);
    }

    // 가게 이름으로 검색
    @Operation(summary = "가게 이름으로 맛집 검색", description = "키워드와 캠퍼스로 가게 이름을 검색한다.")
    @GetMapping("/search")
    public ApiResponse<List<PlaceSearchResponseDto>> search(
            @RequestParam String keyword,
            @RequestParam Campus campus) {
        List<PlaceSearchResponseDto> places = placeReadService.searchPlaceDetails(keyword, campus);
        return ApiResponse.success(places);
    }

    // 메뉴 이름으로 가게 검색
    @Operation(summary = "메뉴 이름으로 맛집 검색", description = "키워드와 캠퍼스로 메뉴 이름을 검색해 해당 메뉴를 파는 가게를 조회한다.")
    @GetMapping("/search/menu")
    public ApiResponse<List<PlaceMenuSearchResponseDto>> searchByMenu(
            @RequestParam String keyword,
            @RequestParam Campus campus) {
        List<PlaceMenuSearchResponseDto> results = placeReadService.searchPlaceByMenuName(keyword, campus);
        return ApiResponse.success(results);
    }
}
