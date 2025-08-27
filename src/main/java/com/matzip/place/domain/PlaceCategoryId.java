package com.matzip.place.domain;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@EqualsAndHashCode
public class PlaceCategoryId implements Serializable {
    private Long place;
    private Long category;
}
