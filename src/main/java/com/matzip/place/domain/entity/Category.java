package com.matzip.place.domain.entity;

import com.matzip.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseEntity {

    @Column(name = "category_name", length = 10)
    private String name;

    @Column(nullable = false)
    private String iconKey;
}