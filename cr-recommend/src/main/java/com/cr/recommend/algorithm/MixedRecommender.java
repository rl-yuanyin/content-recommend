package com.cr.recommend.algorithm;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mixed image recommender.
 *
 * <p>Weights are intentionally biased toward the project's core image-content
 * algorithm:</p>
 * <pre>
 * image content = 50%
 * collaborative filtering = 30%
 * hot recommendation = 20%
 * </pre>
 *
 * <p>Each source list is converted to normalized rank scores. Items appearing
 * in multiple source lists receive an accumulated boost, and duplicates are
 * removed before the Top-N result is returned.</p>
 */
@Component
@RequiredArgsConstructor
public class MixedRecommender {

    private static final double IMAGE_CONTENT_WEIGHT = 0.5D;

    private static final double CF_WEIGHT = 0.3D;

    private static final double HOT_WEIGHT = 0.2D;

    private final ImageContentRecommender imageContentRecommender;

    private final CollaborativeFilteringRecommender collaborativeFilteringRecommender;

    private final HotRecommender hotRecommender;

    /**
     * Produces mixed image recommendations.
     *
     * @param userId user identifier
     * @param count result count
     * @param userItemMatrix user-image rating matrix
     * @param images image statistics for hot ranking
     * @return mixed recommendation identifiers
     */
    public List<Long> recommend(
            Long userId,
            int count,
            Map<Long, Map<Long, Double>> userItemMatrix,
            List<Map<String, Object>> images) {
        if (count <= 0) {
            return new ArrayList<>();
        }

        int sourceCount = Math.max(count * 2, count);
        List<Long> imageResults = imageContentRecommender.recommend(userId, sourceCount);
        List<Long> cfResults = collaborativeFilteringRecommender.recommend(
                userId,
                sourceCount,
                userItemMatrix
        );
        List<Long> hotResults = hotRecommender.recommend(sourceCount, images);

        Map<Long, Double> combinedScores = new HashMap<>();
        addWeightedRankScores(combinedScores, imageResults, IMAGE_CONTENT_WEIGHT);
        addWeightedRankScores(combinedScores, cfResults, CF_WEIGHT);
        addWeightedRankScores(combinedScores, hotResults, HOT_WEIGHT);

        return combinedScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(count)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private void addWeightedRankScores(
            Map<Long, Double> target,
            List<Long> rankedImageIds,
            double weight) {
        if (rankedImageIds == null || rankedImageIds.isEmpty()) {
            return;
        }
        int size = rankedImageIds.size();
        for (int index = 0; index < size; index++) {
            double normalizedRank = (size - index) / (double) size;
            target.merge(rankedImageIds.get(index), normalizedRank * weight, Double::sum);
        }
    }
}
