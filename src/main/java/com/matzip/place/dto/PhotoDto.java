package com.matzip.place.dto;

import com.matzip.place.domain.entity.Photo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PhotoDto {

    @Schema(description = "사진 ID", example = "101")
    private Long photoId;

    @Schema(description = "사진 URL", example = "https://example.com/spots/photo1.jpg")
    private String photoUrl;

    @Schema(description = "노출 순서(0부터)", example = "0")
    private Integer displayOrder;

    public static PhotoDto from(Photo photo) {
        return PhotoDto.builder()
                .photoId(photo.getId())
                .photoUrl(photo.getPhotoUrl())
                .displayOrder(photo.getDisplayOrder())
                .build();
    }

}
