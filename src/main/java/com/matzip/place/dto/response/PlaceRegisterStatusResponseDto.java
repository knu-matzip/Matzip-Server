package com.matzip.place.dto.response;

import com.matzip.place.domain.entity.Place;
import com.matzip.place.dto.CategoryDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class PlaceRegisterStatusResponseDto {

    @Schema(description = "맛집 ID", example = "15")
    private Long placeId;

    @Schema(description = "맛집 이름", example = "우돈탄 다산본점")
    private String placeName;

    @Schema(description = "등록 요청일", example = "2025-08-12")
    private LocalDate requestDate;

    private List<CategoryDto> categories;

    @Schema(description = "등록 상태", example = "PENDING")
    private String registerStatus;

    public static PlaceRegisterStatusResponseDto from(Place place) {
        return PlaceRegisterStatusResponseDto.builder()
                .placeId(place.getId())
                .placeName(place.getName())
                .requestDate(place.getCreatedAt().toLocalDate())
                .categories(place.getCategories().stream()
                        .map(CategoryDto::from)
                        .collect(Collectors.toList()))
                .registerStatus(place.getStatus().name())
                .build();
    }
}
