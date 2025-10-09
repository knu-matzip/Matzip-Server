package com.matzip.place.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "category")
@Getter
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    @Column(name = "category_name", length = 10)
    private String name;

    @Column(name = "icon_key", nullable = false)
    private String iconKey;
}