package com.matzip.common.dto;

import com.matzip.place.domain.entity.Photo;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PhotoDto {
    private Long photoId;
    private String photoUrl;
    private Integer displayOrder;

    public static PhotoDto from(Photo photo) {
        return PhotoDto.builder()
                .photoId(photo.getId())
                .photoUrl(photo.getPhotoUrl())
                .displayOrder(photo.getDisplayOrder())
                .build();
    }

}
