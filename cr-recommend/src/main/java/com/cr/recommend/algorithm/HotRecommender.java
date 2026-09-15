package com.cr.recommend.algorithm;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Hot-image fallback recommender.
 *
 * <p>The ranking score is:</p>
 * <pre>
 * hotScore = viewCount * 0.3 + likeCount * 1.0
 *          + collectCount * 2.0 + downloadCount * 1.5
 * </pre>
 *
 * <p>Comments and likes carry more weight than views because they require a
 * stronger user action.</p>
 */
@Component
public class HotRecommender {

    /**
     * Recommends the highest-scoring images.
     *
     * @param count maximum number of recommendations
     * @param images image data containing count fields
     * @return recommended image identifiers ordered by hot score
     */
    public List<Long> recommend(int count, List<Map<String, Object>> images) {
        if (count <= 0 || images == null || images.isEmpty()) {
            return new ArrayList<>();
        }

        return images.stream()
                .filter(image -> image != null && imageId(image) != null)
                .sorted(Comparator.comparingDouble(this::hotScore).reversed())
                .limit(count)
                .map(this::imageId)
                .collect(Collectors.toList());
    }

    /**
     * Calculates the weighted hot score.
     *
     * @param image image data
     * @return hot score
     */
    private double hotScore(Map<String, Object> image) {
        double viewCount = numberValue(image, "viewCount", "view_count");
        double likeCount = numberValue(image, "likeCount", "like_count");
        double collectCount = numberValue(image, "collectCount", "collect_count");
        double downloadCount = numberValue(image, "downloadCount", "download_count");
        return viewCount * 0.3D
                + likeCount * 1.0D
                + collectCount * 2.0D
                + downloadCount * 1.5D;
    }

    /**
     * Reads an image identifier from commonly used map keys.
     *
     * @param image image data
     * @return image identifier or null
     */
    private Long imageId(Map<String, Object> image) {
        Object value = firstValue(image, "id", "imageId", "image_id");
        return value instanceof Number ? ((Number) value).longValue() : null;
    }

    /**
     * Reads a numeric count from commonly used camel-case or snake-case keys.
     *
     * @param image image data
     * @param camelCaseKey camel-case key
     * @param snakeCaseKey snake-case key
     * @return numeric value, or zero when absent
     */
    private double numberValue(
            Map<String, Object> image,
            String camelCaseKey,
            String snakeCaseKey) {
        Object value = firstValue(image, camelCaseKey, snakeCaseKey);
        return value instanceof Number ? ((Number) value).doubleValue() : 0.0D;
    }

    /**
     * Returns the first non-null value among the supplied keys.
     *
     * @param image image data
     * @param keys candidate keys
     * @return first non-null value or null
     */
    private Object firstValue(Map<String, Object> image, String... keys) {
        for (String key : keys) {
            Object value = image.get(key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }
}
