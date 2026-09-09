package com.matzip.place.service;

import com.matzip.place.domain.DailyViewCount;
import com.matzip.place.repository.DailyViewCountRepository;
import com.matzip.place.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ViewCountService {

    private final PlaceRepository placeRepository;
    private final DailyViewCountRepository dailyViewCountRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementAllCounts(Long placeId) {
        placeRepository.incrementViewCount(placeId);
        incrementDailyViewCount(placeId);
    }

    private void incrementDailyViewCount(Long placeId) {
        LocalDate today = LocalDate.now();
        int updatedRows = dailyViewCountRepository.incrementDailyViewCount(placeId, today);

        if (updatedRows == 0) {
            placeRepository.findById(placeId).ifPresent(place -> {
                DailyViewCount newDailyViewCount = new DailyViewCount(place, today, 1);
                dailyViewCountRepository.save(newDailyViewCount);
            });
        }
    }
}
