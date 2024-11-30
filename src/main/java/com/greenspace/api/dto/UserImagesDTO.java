package com.greenspace.api.dto;

import java.util.UUID;

import com.greenspace.api.enums.ImageType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserImagesDTO {
    private UUID id;
    private String imageUrl;
    private ImageType imageType;
}
