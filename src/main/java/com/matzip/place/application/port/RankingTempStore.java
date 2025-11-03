package com.matzip.place.application.port;

import com.matzip.place.api.response.PlaceRankingResponseDto;
import com.matzip.place.domain.Campus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RankingTempStore {

    Optional<List<PlaceRankingResponseDto>> get(LocalDate date, Campus campus);

    void set(LocalDate date, Campus campus, List<PlaceRankingResponseDto> rankingResponseDtos);

    void remove(LocalDate date, Campus campus);
}
