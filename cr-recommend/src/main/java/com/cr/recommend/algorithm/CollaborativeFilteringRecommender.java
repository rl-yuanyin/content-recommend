package com.cr.recommend.algorithm;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * User-based collaborative filtering recommender.
 *
 * <p>User similarity is calculated with cosine similarity:</p>
 * <pre>
 * sim(u, v) = sum(r(u, i) * r(v, i)) /
 *             (sqrt(sum(r(u, i)^2)) * sqrt(sum(r(v, i)^2)))
 * </pre>
 *
 * <p>The recommender selects the K most similar users and predicts a score for
 * each unseen image using a similarity-weighted average:</p>
 * <pre>
 * score(u, i) = sum(sim(u, v) * r(v, i)) / sum(abs(sim(u, v)))
 * </pre>
 *
 * <p>Behavior scores passed into this class should be: view=1, like=3,
 * comment=5, and favorite=4.</p>
 */
@Component
public class CollaborativeFilteringRecommender {

    private static final int NEIGHBOR_COUNT = 10;

    /**
     * Recommends images using user-based collaborative filtering.
     *
     * @param userId target user identifier
     * @param count maximum number of recommendations
     * @param userItemMatrix user-item rating matrix
     * @return recommended image identifiers ordered by predicted score
     */
    public List<Long> recommend(
            Long userId,
            int count,
            Map<Long, Map<Long, Double>> userItemMatrix) {
        if (count <= 0 || userId == null || userItemMatrix == null) {
            return new ArrayList<>();
        }

        Map<Long, Double> targetRatings = userItemMatrix.get(userId);
        if (targetRatings == null || targetRatings.isEmpty()) {
            return new ArrayList<>();
        }

        List<UserSimilarity> similarities = new ArrayList<>();
        for (Map.Entry<Long, Map<Long, Double>> entry : userItemMatrix.entrySet()) {
            Long otherUserId = entry.getKey();
            if (userId.equals(otherUserId)) {
                continue;
            }
            double similarity = cosineSimilarity(targetRatings, entry.getValue());
            if (similarity > 0.0D) {
                similarities.add(new UserSimilarity(otherUserId, similarity));
            }
        }

        similarities.sort(
                Comparator.comparingDouble(UserSimilarity::getSimilarity).reversed()
        );
        if (similarities.size() > NEIGHBOR_COUNT) {
            similarities = similarities.subList(0, NEIGHBOR_COUNT);
        }

        Map<Long, Double> weightedScores = new HashMap<>();
        Map<Long, Double> similarityTotals = new HashMap<>();
        for (UserSimilarity similarUser : similarities) {
            Map<Long, Double> neighborRatings =
                    userItemMatrix.get(similarUser.getUserId());
            if (neighborRatings == null) {
                continue;
            }
            for (Map.Entry<Long, Double> rating : neighborRatings.entrySet()) {
                Long imageId = rating.getKey();
                if (targetRatings.containsKey(imageId)) {
                    continue;
                }
                weightedScores.merge(
                        imageId,
                        similarUser.getSimilarity() * rating.getValue(),
                        Double::sum
                );
                similarityTotals.merge(
                        imageId,
                        Math.abs(similarUser.getSimilarity()),
                        Double::sum
                );
            }
        }

        Map<Long, Double> predictions = new HashMap<>();
        for (Map.Entry<Long, Double> entry : weightedScores.entrySet()) {
            double denominator = similarityTotals.getOrDefault(entry.getKey(), 0.0D);
            if (denominator > 0.0D) {
                predictions.put(entry.getKey(), entry.getValue() / denominator);
            }
        }

        return predictions.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(count)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Calculates cosine similarity between two users' item-rating vectors.
     *
     * @param left first user's ratings
     * @param right second user's ratings
     * @return cosine similarity
     */
    private double cosineSimilarity(
            Map<Long, Double> left,
            Map<Long, Double> right) {
        if (left == null || right == null || left.isEmpty() || right.isEmpty()) {
            return 0.0D;
        }

        Map<Long, Double> smaller = left.size() <= right.size() ? left : right;
        Map<Long, Double> larger = smaller == left ? right : left;
        double dotProduct = 0.0D;
        for (Map.Entry<Long, Double> entry : smaller.entrySet()) {
            Double otherValue = larger.get(entry.getKey());
            if (otherValue != null) {
                dotProduct += entry.getValue() * otherValue;
            }
        }
        if (dotProduct == 0.0D) {
            return 0.0D;
        }

        double leftNorm = ratingNorm(left);
        double rightNorm = ratingNorm(right);
        if (leftNorm == 0.0D || rightNorm == 0.0D) {
            return 0.0D;
        }
        return dotProduct / (leftNorm * rightNorm);
    }

    /**
     * Calculates the Euclidean norm of a rating vector.
     *
     * @param ratings user ratings
     * @return vector norm
     */
    private double ratingNorm(Map<Long, Double> ratings) {
        double sumOfSquares = 0.0D;
        for (Double rating : ratings.values()) {
            sumOfSquares += rating * rating;
        }
        return Math.sqrt(sumOfSquares);
    }

    /**
     * Similar-user score holder.
     */
    private static final class UserSimilarity {

        private final Long userId;

        private final double similarity;

        private UserSimilarity(Long userId, double similarity) {
            this.userId = userId;
            this.similarity = similarity;
        }

        private Long getUserId() {
            return userId;
        }

        private double getSimilarity() {
            return similarity;
        }
    }
}
