package com.matzip.place.dto;

import com.matzip.place.domain.entity.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryDto {

    @Schema(description = "카테고리 ID", example = "5")
    private Long id;

    @Schema(description = "카테고리 이름", example = "고기·구이")
    private String name;

    @Schema(description = "아이콘 키", example = "Meat")
    private String iconKey;

    public static CategoryDto from(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .iconKey(category.getIconKey())
                .build();
    }
}
