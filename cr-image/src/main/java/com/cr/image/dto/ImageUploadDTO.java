package com.cr.image.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Local image storage result.
 */
@Data
public class ImageUploadDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String url;

    private String thumbnailUrl;

    private Integer width;

    private Integer height;

    private Long fileSize;
}
