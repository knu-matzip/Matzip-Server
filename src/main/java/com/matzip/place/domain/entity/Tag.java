package com.matzip.place.domain.entity;

import com.matzip.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag extends BaseEntity {

    @Column(name = "tag_name", length = 20)
    private String name;

    @Column(nullable = false)
    private String iconKey;
}
