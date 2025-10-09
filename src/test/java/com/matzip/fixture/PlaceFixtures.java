package com.matzip.fixture;

import com.matzip.place.domain.Campus;
import com.matzip.place.domain.PlaceStatus;
import com.matzip.place.domain.entity.Place;
import com.matzip.user.domain.User;

import java.util.Random;

public class PlaceFixtures {

    private PlaceFixtures() {
    }

    public static Place createPlaceWith(String identifier, User registrant) {
        return Place.builder()
                .campus(Campus.SINGWAN)
                .kakaoPlaceId(identifier)
                .name(identifier)
                .address("address " + identifier)
                .latitude(new Random().nextDouble(36, 37))
                .longitude(new Random().nextDouble(126, 127))
                .description("description " + identifier)
                .registeredBy(registrant)
                .status(PlaceStatus.APPROVED)
                .build();
    }
}
