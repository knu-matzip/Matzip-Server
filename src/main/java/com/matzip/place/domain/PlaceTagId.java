package com.matzip.place.domain;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@EqualsAndHashCode
public class PlaceTagId implements Serializable {
    private Long place;
    private Integer tag;
}
