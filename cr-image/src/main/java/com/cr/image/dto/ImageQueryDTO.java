package com.cr.image.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Image list query.
 */
@Data
public class ImageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String keyword;

    private Long categoryId;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
