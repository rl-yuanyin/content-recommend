package com.cr.recommend.algorithm;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cr.recommend.entity.UserBehavior;
import com.cr.recommend.mapper.UserBehaviorMapper;
import com.cr.recommend.service.MilvusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Image-content recommender based on deep features and Milvus ANN search.
 *
 * <p>For each of the user's three most recent viewed images, the recommender
 * obtains its 2048-dimensional feature vector from Milvus and retrieves the
 * Top-20 nearest images. Results are merged with a recency weight:</p>
 * <pre>
 * sourceWeight = 1.0, 0.8, 0.6
 * candidateScore += sourceWeight / (rank + 1)
 * </pre>
 *
 * <p>Images already viewed, liked, commented on, collected, or downloaded by
 * the user are excluded from the final result.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ImageContentRecommender {

    private static final int RECENT_IMAGE_COUNT = 3;

    private static final int SIMILAR_IMAGE_COUNT = 20;

    private static final double[] RECENCY_WEIGHTS = {1.0D, 0.8D, 0.6D};

    private final MilvusService milvusService;

    private final UserBehaviorMapper userBehaviorMapper;

    /**
     * Recommends images similar to the user's recent visual preferences.
     *
     * @param userId user identifier
     * @param count maximum result count
     * @return recommended image identifiers
     */
    public List<Long> recommend(Long userId, int count) {
        if (userId == null || count <= 0) {
            return new ArrayList<>();
        }

        List<UserBehavior> recentBehaviors = userBehaviorMapper.selectList(
                new LambdaQueryWrapper<UserBehavior>()
                        .eq(UserBehavior::getUserId, userId)
                        .eq(UserBehavior::getBehaviorType, 1)
                        .orderByDesc(UserBehavior::getCreateTime)
                        .last("LIMIT " + RECENT_IMAGE_COUNT)
        );
        if (recentBehaviors.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Long> excludedImageIds = userBehaviorMapper.selectList(
                        new LambdaQueryWrapper<UserBehavior>()
                                .eq(UserBehavior::getUserId, userId)
                ).stream()
                .map(UserBehavior::getImageId)
                .filter(imageId -> imageId != null)
                .collect(Collectors.toSet());

        Map<Long, Double> scores = new HashMap<>();
        for (int sourceIndex = 0; sourceIndex < recentBehaviors.size(); sourceIndex++) {
            Long sourceImageId = recentBehaviors.get(sourceIndex).getImageId();
            if (sourceImageId == null) {
                continue;
            }
            try {
                float[] sourceVector = milvusService.getVector(sourceImageId);
                if (sourceVector == null) {
                    continue;
                }
                List<Long> similarImages = milvusService.searchSimilar(
                        sourceVector,
                        SIMILAR_IMAGE_COUNT,
                        null
                );
                double recencyWeight = RECENCY_WEIGHTS[
                        Math.min(sourceIndex, RECENCY_WEIGHTS.length - 1)
                ];
                mergeRankedResults(scores, similarImages, recencyWeight);
            } catch (Exception exception) {
                log.warn("Failed to search similar images for image {}", sourceImageId, exception);
            }
        }

        return scores.entrySet().stream()
                .filter(entry -> !excludedImageIds.contains(entry.getKey()))
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(count)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private void mergeRankedResults(
            Map<Long, Double> scores,
            List<Long> imageIds,
            double sourceWeight) {
        if (imageIds == null || imageIds.isEmpty()) {
            return;
        }
        for (int rank = 0; rank < imageIds.size(); rank++) {
            Long imageId = imageIds.get(rank);
            scores.merge(imageId, sourceWeight / (rank + 1.0D), Double::sum);
        }
    }
}
