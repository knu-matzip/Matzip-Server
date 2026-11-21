package com.matzip.place.event;

import com.matzip.common.config.AsyncConfiguration;
import com.matzip.place.service.ViewCountService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PlaceViewedEventListener {

    private final ViewCountService viewCountService;

    // 원본 로그가 커밋된 조회만 집계해야 집계가 로그보다 많아지지 않는다
    @Async(AsyncConfiguration.GENERAL_EXECUTOR)
    @TransactionalEventListener
    public void incrementViewCounts(PlaceViewedEvent event) {
        viewCountService.incrementAllCounts(event.placeId(), event.viewDate());
    }
}
