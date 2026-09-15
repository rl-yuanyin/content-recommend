package com.cr.recommend.algorithm;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Focused tests for the recommendation algorithms.
 */
class RecommendAlgorithmTest {

    @Test
    void hotRecommenderShouldUseWeightedScore() {
        Map<String, Object> first = image(1L, 100, 0, 0, 0);
        Map<String, Object> second = image(2L, 0, 31, 0, 0);
        Map<String, Object> third = image(3L, 10, 10, 10, 10);

        List<Long> result = new HotRecommender().recommend(
                3,
                Arrays.asList(first, second, third)
        );

        assertEquals(Arrays.asList(3L, 2L, 1L), result);
    }

    @Test
    void collaborativeFilteringShouldRecommendNeighborItems() {
        Map<Long, Map<Long, Double>> matrix = new HashMap<>();
        matrix.put(1L, ratings(1L, 3.0D, 2L, 1.0D));
        matrix.put(2L, ratings(1L, 3.0D, 3L, 5.0D));
        matrix.put(3L, ratings(4L, 5.0D));

        List<Long> result = new CollaborativeFilteringRecommender().recommend(
                1L,
                2,
                matrix
        );

        assertFalse(result.isEmpty());
        assertEquals(3L, result.get(0));
    }

    private Map<String, Object> image(
            Long id,
            int viewCount,
            int likeCount,
            int collectCount,
            int downloadCount) {
        Map<String, Object> image = new HashMap<>();
        image.put("id", id);
        image.put("viewCount", viewCount);
        image.put("likeCount", likeCount);
        image.put("collectCount", collectCount);
        image.put("downloadCount", downloadCount);
        return image;
    }

    private Map<Long, Double> ratings(Object... values) {
        Map<Long, Double> ratings = new HashMap<>();
        for (int index = 0; index < values.length; index += 2) {
            ratings.put((Long) values[index], (Double) values[index + 1]);
        }
        return ratings;
    }
}
