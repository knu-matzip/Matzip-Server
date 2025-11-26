package com.matzip.place.api.response;

import com.matzip.place.domain.entity.Place;
import com.matzip.place.dto.CategoryDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class PlaceRegisterStatusResponseDto {

    private Long placeId;
    private String placeName;
    private LocalDate requestDate;
    private List<CategoryDto> categories;
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
