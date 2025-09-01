package com.matzip.place.api;

import com.matzip.common.response.ApiResponse;
import com.matzip.place.api.request.PlaceCheckRequestDto;
import com.matzip.place.api.request.PlaceRequestDto;
import com.matzip.place.api.response.PlaceCheckResponseDto;
import com.matzip.place.api.response.PlaceRegisterResponseDto;
import com.matzip.place.application.PlaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/places")
public class PlaceController {

    private final PlaceService placeService;

    // 프리뷰
    @GetMapping("/preview")
    public ApiResponse<PlaceCheckResponseDto> preview(@Valid @ModelAttribute PlaceCheckRequestDto req) {
        PlaceCheckResponseDto data = placeService.preview(req); // 서비스 결과 DTO
        return ApiResponse.success(data);                       // 공통 응답 형태로 래핑
    }

    // 등록
    @PostMapping
    public ApiResponse<PlaceRegisterResponseDto> register(@Valid @RequestBody PlaceRequestDto req) {
        PlaceRegisterResponseDto data = placeService.register(req);
        return ApiResponse.success(data);
    }
}
