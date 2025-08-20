package com.matzip.common.dto;

import com.matzip.place.domain.Tag;
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
