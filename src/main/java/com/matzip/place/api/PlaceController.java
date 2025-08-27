package com.matzip.place.api;

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
    public PlaceCheckResponseDto preview(@Valid @ModelAttribute PlaceCheckRequestDto req) {
        return placeService.preview(req);
    }

    // 등록
    @PostMapping
    public PlaceRegisterResponseDto register(@Valid @RequestBody PlaceRequestDto req) {
        return placeService.register(req);
    }
}
