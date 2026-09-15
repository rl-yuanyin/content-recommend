package com.cr.common.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * AI prediction result for an uploaded image.
 */
@Data
public class ImagePredictionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long categoryId;

    private String categoryName;

    private String titleSuggestion;

    private String descriptionSuggestion;

    private String tagsSuggestion;

    private Long similarImageId;

    private Double similarity;
}
