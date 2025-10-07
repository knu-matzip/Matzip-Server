package com.matzip.lottery.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Getter;

@Getter
@Embeddable
public class Prize {

    @Column(name = "prize_description", nullable = false)
    private String description;

    @Column(name = "prize_image_url", nullable = false)
    private String imageUrl;

    protected Prize() {
    }

    @Builder
    public Prize(String description, String imageUrl) {
        this.description = description;
        this.imageUrl = imageUrl;
    }
}
