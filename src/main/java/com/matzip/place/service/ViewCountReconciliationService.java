package com.matzip.place.service;

import com.matzip.place.domain.DailyViewCount;
import com.matzip.place.domain.entity.Place;
import com.matzip.place.domain.entity.ViewCountReconciliation;
import com.matzip.place.dto.PlaceViewTallyDto;
import com.matzip.place.repository.DailyViewCountRepository;
import com.matzip.place.repository.PlaceRepository;
import com.matzip.place.repository.PlaceViewLogRepository;
import com.matzip.place.repository.ViewCountReconciliationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 원본 조회 로그(place_view_log)를 기준으로 하루치 조회수를 검증하고 보정한다.
 * 보정 후에는 집계와 로그가 같아지므로 같은 날짜를 다시 보정해도 delta는 0이다.
 */
@Service
@RequiredArgsConstructor
public class ViewCountReconciliationService {

    private final PlaceViewLogRepository placeViewLogRepository;
    private final DailyViewCountRepository dailyViewCountRepository;
    private final PlaceRepository placeRepository;
    private final ViewCountReconciliationRepository viewCountReconciliationRepository;

    @Transactional
    public ViewCountReconciliation reconcile(LocalDate date) {
        Map<Long, Long> logged = toCountMap(placeViewLogRepository.countByPlaceOn(date));
        Map<Long, Long> aggregated = toCountMap(dailyViewCountRepository.sumByPlaceOn(date));

        Set<Long> placeIds = new HashSet<>(logged.keySet());
        placeIds.addAll(aggregated.keySet());
        // 삭제된 맛집은 다시 넣을 대상이 없으므로 비교에서 제외한다
        Map<Long, Place> places = placeRepository.findAllById(placeIds).stream()
                .collect(Collectors.toMap(Place::getId, Function.identity()));

        long loggedTotal = 0;
        long aggregatedTotal = 0;
        long missing = 0;
        long over = 0;
        List<DailyViewCount> corrected = new ArrayList<>();
        Map<Long, Long> deltas = new HashMap<>();

        for (Place place : places.values()) {
            long logCount = logged.getOrDefault(place.getId(), 0L);
            long aggCount = aggregated.getOrDefault(place.getId(), 0L);
            loggedTotal += logCount;
            aggregatedTotal += aggCount;

            long delta = logCount - aggCount;
            if (delta > 0) {
                missing += delta;
            } else if (delta < 0) {
                over += -delta;
            }
            if (delta != 0) {
                deltas.put(place.getId(), delta);
            }
            if (logCount > 0) {
                corrected.add(new DailyViewCount(place, date, Math.toIntExact(logCount)));
            }
        }

        // 중복 행까지 한 번에 정리하려고 해당 날짜를 지우고 로그 기준으로 place당 1행씩 다시 넣는다
        dailyViewCountRepository.deleteByViewDate(date);
        dailyViewCountRepository.saveAll(corrected);
        deltas.forEach(placeRepository::addViewCount);

        return viewCountReconciliationRepository.save(
                new ViewCountReconciliation(date, loggedTotal, aggregatedTotal, missing, over));
    }

    @Transactional(readOnly = true)
    public Optional<LocalDate> findLastReconciledDate() {
        return viewCountReconciliationRepository.findTopByOrderByTargetDateDesc()
                .map(ViewCountReconciliation::getTargetDate);
    }

    @Transactional
    public int purgeLogsBefore(LocalDate date) {
        return placeViewLogRepository.deleteByViewDateBefore(date);
    }

    private Map<Long, Long> toCountMap(List<PlaceViewTallyDto> tallies) {
        return tallies.stream()
                .collect(Collectors.toMap(PlaceViewTallyDto::placeId, PlaceViewTallyDto::count));
    }
}
