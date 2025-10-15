package com.matzip.place.infra.scheduler;

import com.matzip.place.api.response.PlaceRankingResponseDto;
import com.matzip.place.application.port.RankingTempStore;
import com.matzip.place.application.service.PlaceReadService;
import com.matzip.place.domain.Campus;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PlaceRankingScheduler {

    private final PlaceReadService placeReadService;
    private final RankingTempStore rankingTempStore;

    @Scheduled(cron = "0 5 0 * * *")
    public void cachePreviousDayPlaceRankings() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate twoDaysAgo = LocalDate.now().minusDays(2);

        Arrays.stream(Campus.values()).forEach(campus -> {
            List<PlaceRankingResponseDto> ranking = placeReadService.getDailyRankingByViews(campus, yesterday);
            rankingTempStore.set(yesterday, campus, ranking);

            rankingTempStore.remove(twoDaysAgo, campus);
        });
    }
}
