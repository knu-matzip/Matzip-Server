package com.matzip.place.dto;

import com.matzip.place.domain.entity.Tag;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TagDto {

    private Long id;
    private String name;
    private String iconKey;

    public static TagDto from(Tag tag) {
        return TagDto.builder()
                .id(tag.getId())
                .name(tag.getName())
                .iconKey(tag.getIconKey())
                .build();
    }
}
