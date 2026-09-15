package com.cr.recommend.service;

import com.cr.common.dto.ImagePredictionDTO;

import java.util.List;
import java.util.Map;

/**
 * Multimodal image understanding service.
 */
public interface LLMService {

    /**
     * Understands an image and produces metadata grounded by retrieved images.
     *
     * @param imageBytes uploaded image bytes
     * @param contentType uploaded image media type
     * @param ragContexts metadata retrieved through vector similarity
     * @param categories allowed category identifiers and names
     * @return generated image metadata
     */
    ImagePredictionDTO analyzeImage(
            byte[] imageBytes,
            String contentType,
            List<Map<String, Object>> ragContexts,
            Map<Long, String> categories);
}
