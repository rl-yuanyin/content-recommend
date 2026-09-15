package com.cr.image.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

/**
 * Image upload or update request.
 */
@Data
public class ImageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String title;

    private String description;

    private Long categoryId;

    /**
     * Comma-separated image tags.
     */
    private String tags;

    /**
     * Uploaded image file.
     */
    private MultipartFile file;
}
