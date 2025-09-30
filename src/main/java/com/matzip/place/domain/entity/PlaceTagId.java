package com.matzip.place.domain.entity;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@EqualsAndHashCode
public class PlaceTagId implements Serializable {
    private Long place;
    private Long tag;
}
