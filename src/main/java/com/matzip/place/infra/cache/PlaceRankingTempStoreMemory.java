package com.matzip.place.infra.cache;

import com.matzip.place.api.response.PlaceRankingResponseDto;
import com.matzip.place.application.port.RankingTempStore;
import com.matzip.place.domain.Campus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PlaceRankingTempStoreMemory implements RankingTempStore {

    private final Map<String, List<PlaceRankingResponseDto>> store = new ConcurrentHashMap<>();


    @Override
    public Optional<List<PlaceRankingResponseDto>> get(LocalDate date, Campus campus) {
        String key = generateKey(date, campus);
        return Optional.ofNullable(store.get(key));
    }

    @Override
    public void set(LocalDate date, Campus campus, List<PlaceRankingResponseDto> rankingResponseDtos) {
        String key = generateKey(date, campus);
        store.put(key, rankingResponseDtos);
    }

    @Override
    public void remove(LocalDate date, Campus campus) {
        String key = generateKey(date, campus);
        store.remove(key);
    }

    private String generateKey(LocalDate date, Campus campus) {
        return date.toString() + ":" + campus.name();
    }
}
