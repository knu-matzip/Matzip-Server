package com.matzip.common.dto;

import com.matzip.place.domain.Category;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryDto {
    private Long id;
    private String name;
    private String iconKey;

    public static CategoryDto from(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .iconKey(category.getIconKey())
                .build();
    }
}
