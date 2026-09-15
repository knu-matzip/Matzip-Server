package com.matzip.place.dto;

import com.matzip.place.domain.entity.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TagDto {

    @Schema(description = "태그 ID", example = "3")
    private Long id;

    @Schema(description = "태그 이름", example = "혼밥하기 좋은")
    private String name;

    @Schema(description = "아이콘 키", example = "fingerUp")
    private String iconKey;

    public static TagDto from(Tag tag) {
        return TagDto.builder()
                .id(tag.getId())
                .name(tag.getName())
                .iconKey(tag.getIconKey())
                .build();
    }
}
