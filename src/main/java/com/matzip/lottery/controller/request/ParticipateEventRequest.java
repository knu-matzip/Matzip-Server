package com.matzip.lottery.controller.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ParticipateEventRequest(@NotNull Long eventId, @Positive int ticketsCount) {
}
