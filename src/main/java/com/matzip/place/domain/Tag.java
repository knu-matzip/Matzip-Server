package com.matzip.place.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "tag")
@Getter
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long id;

    @Column(name = "tag_name", length = 20)
    private String name;

    @Column(name = "icon_key", nullable = false)
    private String iconKey;
}
