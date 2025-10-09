package com.matzip.user.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProfileImage {
    CAT("/images/profiles/cat.png"),
    BEAR("/images/profiles/bear.png"),
    CHICKEN("/images/profiles/chicken.png"),
    COW("/images/profiles/cow.png"),
    DRAGON("/images/profiles/dragon.png"),
    FOX("/images/profiles/fox.png"),
    FROG("/images/profiles/fog.png"),
    HAMSTER("/images/profiles/hamster.png"),
    HORSE("/images/profiles/horse.png"),
    KOALA("/images/profiles/koala.png"),
    LION("/images/profiles/lion.png"),
    MONKEY("/images/profiles/monkey.png"),
    MOUSE("/images/profiles/mouse.png"),
    PANDA("/images/profiles/panda.png"),
    PENGUIN("/images/profiles/penguin.png"),
    PIG("/images/profiles/pig.png"),
    RABBIT("/images/profiles/rabbit.png"),
    TIGER("/images/profiles/tiger.png"),
    UNICORN("/images/profiles/unicon.png");

    private final String imageUrl;

    public String getFullUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isEmpty()) {
            return imageUrl;
        }
        return baseUrl + imageUrl;
    }
}
