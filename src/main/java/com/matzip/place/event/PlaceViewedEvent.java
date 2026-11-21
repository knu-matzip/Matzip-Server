package com.matzip.place.event;

import java.time.LocalDate;

public record PlaceViewedEvent(Long placeId, LocalDate viewDate) {
}
