package com.cr.recommend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cr.common.result.Result;
import com.cr.common.dto.ImagePredictionDTO;
import com.cr.common.result.ResultCode;
import com.cr.recommend.algorithm.CollaborativeFilteringRecommender;
import com.cr.recommend.algorithm.HotRecommender;
import com.cr.recommend.algorithm.ImageContentRecommender;
import com.cr.recommend.algorithm.MixedRecommender;
import com.cr.recommend.config.RecommendProperties;
import com.cr.recommend.entity.RecommendResult;
import com.cr.recommend.entity.UserBehavior;
import com.cr.recommend.feign.ImageFeignClient;
import com.cr.recommend.mapper.RecommendResultMapper;
import com.cr.recommend.mapper.UserBehaviorMapper;
import com.cr.recommend.service.FeatureExtractService;
import com.cr.recommend.service.LLMService;
import com.cr.recommend.service.MilvusService;
import com.cr.recommend.service.RecommendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Image recommendation orchestration service.
 *
 * <p>The service combines deep image-content similarity from Milvus,
 * collaborative filtering, and hot-image ranking. Results are cached in Redis.
 * Sparse user data automatically falls back to hot images.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendServiceImpl implements RecommendService {

    private static final String CACHE_PREFIX = "recommend:";

    private static final int MIN_BEHAVIOR_COUNT = 3;

    private static final int DEFAULT_RECOMMEND_COUNT = 10;

    private static final int MAX_RECOMMEND_COUNT = 100;

    private static final String IMAGE_STATISTICS_SQL =
            "SELECT id, title, description, url, category_id, "
                    + "view_count AS viewCount, "
                    + "like_count AS likeCount, "
                    + "collect_count AS collectCount, "
                    + "download_count AS downloadCount, "
                    + "feature_extracted AS featureExtracted "
                    + "FROM image WHERE status = 1";

    private final ImageContentRecommender imageContentRecommender;

    private final CollaborativeFilteringRecommender collaborativeFilteringRecommender;

    private final HotRecommender hotRecommender;

    private final MixedRecommender mixedRecommender;

    private final UserBehaviorMapper userBehaviorMapper;

    private final RecommendResultMapper recommendResultMapper;

    private final RedisTemplate<String, Object> redisTemplate;

    private final JdbcTemplate jdbcTemplate;

    private final RecommendProperties recommendProperties;

    private final FeatureExtractService featureExtractService;

    private final LLMService llmService;

    private final MilvusService milvusService;

    private final ImageFeignClient imageFeignClient;

    /**
     * Gets mixed image recommendations using the configured cache.
     *
     * @param userId user identifier
     * @param count recommendation count
     * @return recommended image identifiers
     */
    @Override
    public Result getRecommend(Long userId, Integer count) {
        if (userId == null) {
            return Result.fail(ResultCode.BAD_REQUEST.getCode(), "用户ID不能为空");
        }

        int resultCount = normalizeCount(count, DEFAULT_RECOMMEND_COUNT);
        String cacheKey = mixedCacheKey(userId);
        List<Long> cached = getCachedRecommendations(cacheKey);
        if (cached != null) {
            return Result.success(cached);
        }

        List<Long> recommendations = buildMixedRecommendations(userId, resultCount);
        cacheRecommendations(cacheKey, recommendations);
        return Result.success(recommendations);
    }

    /**
     * Gets image-content recommendations and falls back to hot images.
     *
     * @param userId user identifier
     * @param count recommendation count
     * @return recommended image identifiers
     */
    @Override
    public Result getContentBasedRecommend(Long userId, Integer count) {
        if (userId == null) {
            return Result.fail(ResultCode.BAD_REQUEST.getCode(), "用户ID不能为空");
        }

        int resultCount = normalizeCount(
                count,
                recommendProperties.getContentRecommendCount()
        );
        String cacheKey = algorithmCacheKey("image_content", userId);
        List<Long> cached = getCachedRecommendations(cacheKey);
        if (cached != null) {
            return Result.success(cached);
        }

        List<UserBehavior> behaviors = loadUserBehaviors(userId);
        List<Long> recommendations;
        if (behaviors.size() < MIN_BEHAVIOR_COUNT) {
            recommendations = hotRecommender.recommend(
                    resultCount,
                    loadImageStatistics()
            );
        } else {
            recommendations = imageContentRecommender.recommend(userId, resultCount);
            if (recommendations.isEmpty()) {
                recommendations = hotRecommender.recommend(
                        resultCount,
                        loadImageStatistics()
                );
            }
        }

        cacheRecommendations(cacheKey, recommendations);
        return Result.success(recommendations);
    }

    /**
     * Gets collaborative-filtering recommendations and falls back to hot data.
     *
     * @param userId user identifier
     * @param count recommendation count
     * @return recommended image identifiers
     */
    @Override
    public Result getCFRecommend(Long userId, Integer count) {
        if (userId == null) {
            return Result.fail(ResultCode.BAD_REQUEST.getCode(), "用户ID不能为空");
        }

        int resultCount = normalizeCount(
                count,
                recommendProperties.getCfRecommendCount()
        );
        String cacheKey = algorithmCacheKey("cf", userId);
        List<Long> cached = getCachedRecommendations(cacheKey);
        if (cached != null) {
            return Result.success(cached);
        }

        List<UserBehavior> behaviors = loadUserBehaviors(userId);
        List<Long> recommendations;
        if (behaviors.size() < MIN_BEHAVIOR_COUNT) {
            recommendations = hotRecommender.recommend(
                    resultCount,
                    loadImageStatistics()
            );
        } else {
            recommendations = collaborativeFilteringRecommender.recommend(
                    userId,
                    resultCount,
                    buildUserItemMatrix()
            );
            if (recommendations.isEmpty()) {
                recommendations = hotRecommender.recommend(
                        resultCount,
                        loadImageStatistics()
                );
            }
        }

        cacheRecommendations(cacheKey, recommendations);
        return Result.success(recommendations);
    }

    /**
     * Gets hot image recommendations.
     *
     * @param count recommendation count
     * @return recommended image identifiers
     */
    @Override
    public Result getHotRecommend(Integer count) {
        int resultCount = normalizeCount(
                count,
                recommendProperties.getHotRecommendCount()
        );
        String cacheKey = CACHE_PREFIX + "hot:" + resultCount;
        List<Long> cached = getCachedRecommendations(cacheKey);
        if (cached != null) {
            return Result.success(cached);
        }

        List<Long> recommendations = hotRecommender.recommend(
                resultCount,
                loadImageStatistics()
        );
        cacheRecommendations(cacheKey, recommendations);
        return Result.success(recommendations);
    }

    /**
     * Records an image behavior and invalidates recommendation caches.
     *
     * @param userId user identifier
     * @param imageId image identifier
     * @param behaviorType behavior type
     * @param duration dwell time in seconds
     * @return recording result
     */
    @Override
    public Result recordBehavior(
            Long userId,
            Long imageId,
            Integer behaviorType,
            Integer duration) {
        if (userId == null || imageId == null || !isValidBehaviorType(behaviorType)) {
            return Result.fail(ResultCode.BAD_REQUEST.getCode(), "用户行为参数不合法");
        }

        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setImageId(imageId);
        behavior.setBehaviorType(behaviorType);
        behavior.setDuration(duration == null || duration < 0 ? 0 : duration);
        behavior.setCreateTime(LocalDateTime.now());
        if (userBehaviorMapper.insert(behavior) != 1) {
            return Result.fail("记录用户行为失败");
        }

        redisTemplate.delete(Arrays.asList(
                mixedCacheKey(userId),
                algorithmCacheKey("image_content", userId),
                algorithmCacheKey("cf", userId)
        ));
        return Result.success();
    }

    /**
     * Asynchronously recomputes and persists mixed recommendations.
     *
     * @param userId user identifier
     * @return refresh result
     */
    @Override
    @Async
    public Result refreshRecommend(Long userId) {
        if (userId == null) {
            return Result.fail(ResultCode.BAD_REQUEST.getCode(), "用户ID不能为空");
        }

        try {
            int resultCount = normalizeCount(
                    null,
                    recommendProperties.getContentRecommendCount()
            );
            List<Long> recommendations = buildMixedRecommendations(userId, resultCount);
            cacheRecommendations(mixedCacheKey(userId), recommendations);
            persistRecommendationResult(userId, recommendations);
            return Result.success(recommendations);
        } catch (RuntimeException exception) {
            log.error("Failed to refresh recommendations for user {}", userId, exception);
            return Result.fail("刷新推荐失败");
        }
    }

    /**
     * Manually extracts and stores an image feature.
     *
     * @param imageId image identifier
     * @return feature dimension
     */
    @Override
    public Result extractFeature(Long imageId) {
        if (imageId == null) {
            return Result.fail(ResultCode.BAD_REQUEST.getCode(), "图片ID不能为空");
        }
        try {
            int dimension = extractAndStoreFeature(imageId);
            if (dimension <= 0) {
                return Result.fail("图片特征提取失败");
            }
            return Result.success(dimension);
        } catch (Exception exception) {
            log.error("Failed to extract feature for image {}", imageId, exception);
            return Result.fail("图片特征提取失败");
        }
    }

    /**
     * Initializes Milvus and imports vectors for existing images.
     *
     * @return imported image count
     */
    @Override
    public Result initMilvus() {
        milvusService.initCollection();
        List<Map<String, Object>> images = loadImageStatistics();
        int imported = 0;
        for (Map<String, Object> image : images) {
            Long imageId = longValue(image.get("id"));
            if (imageId == null) {
                continue;
            }
            try {
                if (extractAndStoreFeature(imageId) > 0) {
                    imported++;
                }
            } catch (Exception exception) {
                log.warn("Failed to import feature for image {}", imageId, exception);
            }
        }
        return Result.success(imported);
    }

    /**
     * Predicts metadata with Qwen-VL, grounded by ResNet50/Milvus retrieval.
     *
     * @param file uploaded image file
     * @return prediction result
     */
    @Override
    public Result predictImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.fail(ResultCode.BAD_REQUEST.getCode(), "图片文件不能为空");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            return Result.fail(ResultCode.BAD_REQUEST.getCode(), "仅支持图片文件");
        }

        byte[] imageBytes;
        try {
            imageBytes = file.getBytes();
        } catch (Exception exception) {
            log.warn("Failed to read uploaded file for prediction", exception);
            return Result.fail("图片读取失败");
        }

        List<Map<String, Object>> ragContexts = retrieveRagContexts(imageBytes);
        Map<Long, String> categories = loadCategories();
        try {
            ImagePredictionDTO prediction = llmService.analyzeImage(
                    imageBytes,
                    contentType,
                    ragContexts,
                    categories
            );
            if (!ragContexts.isEmpty()) {
                prediction.setSimilarImageId(longValue(ragContexts.get(0).get("id")));
                prediction.setSimilarity(1.0D);
            }
            return Result.success(prediction);
        } catch (RuntimeException exception) {
            log.warn("Qwen-VL prediction unavailable; using vector metadata fallback: {}",
                    exception.getMessage());
        }

        ImagePredictionDTO fallback = buildVectorFallbackPrediction(ragContexts, categories);
        if (fallback == null) {
            return Result.fail("AI图片识别失败，请检查通义千问API配置后重试");
        }
        return Result.success(fallback);
    }

    private List<Map<String, Object>> retrieveRagContexts(byte[] imageBytes) {
        List<Map<String, Object>> contexts = new ArrayList<>();
        float[] feature;
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes)) {
            feature = featureExtractService.extractFeatureFromInputStream(inputStream);
        } catch (Exception exception) {
            log.warn("Failed to extract RAG feature from uploaded image", exception);
            return contexts;
        }
        if (feature == null) {
            return contexts;
        }

        try {
            List<Long> similarIds = milvusService.searchSimilar(feature, 8, null);
            for (Long similarId : similarIds) {
                Map<String, Object> metadata = loadImageMetadata(similarId);
                if (metadata != null) {
                    contexts.add(metadata);
                }
            }
        } catch (RuntimeException exception) {
            log.warn("Milvus RAG retrieval unavailable: {}", exception.getMessage());
        }
        return contexts;
    }

    private ImagePredictionDTO buildVectorFallbackPrediction(
            List<Map<String, Object>> ragContexts,
            Map<Long, String> categories) {
        if (ragContexts.isEmpty()) {
            return null;
        }
        Map<Long, Integer> categoryVotes = new HashMap<>();
        Map<String, Object> bestMatch = null;
        for (Map<String, Object> metadata : ragContexts) {
            String title = stringValue(metadata.get("title"));
            String tags = stringValue(metadata.get("tags"));
            boolean hasUsefulMeta = !title.isEmpty() && !tags.isEmpty();
            if (bestMatch == null || (hasUsefulMeta && !hasUsefulMetadata(bestMatch))) {
                bestMatch = metadata;
            }
            Long categoryId = longValue(metadata.get("categoryId"));
            if (categoryId != null) {
                categoryVotes.merge(categoryId, 1, Integer::sum);
            }
        }
        if (bestMatch == null) {
            return null;
        }

        Long categoryId = categoryVotes.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(longValue(bestMatch.get("categoryId")));
        String categoryName = categories.get(categoryId);
        if (categoryName == null) {
            categoryName = loadCategoryName(categoryId);
        }
        String bestTitle = stringValue(bestMatch.get("title"));
        String bestTags = stringValue(bestMatch.get("tags"));

        if (bestTitle.isEmpty() || bestTitle.equals("my-image")) {
            bestTitle = categoryName + "图片";
        }

        ImagePredictionDTO prediction = new ImagePredictionDTO();
        prediction.setCategoryId(categoryId);
        prediction.setCategoryName(categoryName);
        prediction.setTitleSuggestion("AI识别 · " + bestTitle);
        if (bestTags.isEmpty()) {
            prediction.setDescriptionSuggestion(
                    "基于图像深度学习特征分析，该图片被识别为["
                            + categoryName
                            + "]类别，通过ResNet50模型提取2048维特征向量并与Milvus向量数据库进行相似度匹配。"
            );
        } else {
            prediction.setDescriptionSuggestion(
                    "基于图像深度学习特征，该图片与["
                            + bestTitle
                            + "]视觉相似，推荐标签："
                            + bestTags
            );
        }
        prediction.setTagsSuggestion(bestTags);
        prediction.setSimilarImageId(longValue(bestMatch.get("id")));
        prediction.setSimilarity(1.0D);
        return prediction;
    }

    private boolean hasUsefulMetadata(Map<String, Object> metadata) {
        return !stringValue(metadata.get("title")).isEmpty()
                && !stringValue(metadata.get("tags")).isEmpty();
    }

    /**
     * Gets similar images and supplements results across categories when needed.
     *
     * @param imageId image identifier
     * @param count result count
     * @return similar image identifiers
     */
    @Override
    public Result getSimilarImages(Long imageId, Integer count) {
        if (imageId == null) {
            return Result.fail(ResultCode.BAD_REQUEST.getCode(), "图片ID不能为空");
        }
        int resultCount = normalizeCount(count, 12);
        String cacheKey = CACHE_PREFIX + "similar:" + imageId + ":" + resultCount;
        List<Long> cached = getCachedRecommendations(cacheKey);
        if (cached != null) {
            return Result.success(cached);
        }

        Long categoryId = loadImageCategoryId(imageId);
        float[] vector = milvusService.getVector(imageId);
        if (vector == null) {
            try {
                extractAndStoreFeature(imageId);
                vector = milvusService.getVector(imageId);
            } catch (Exception exception) {
                log.warn("Failed to build vector for image {}", imageId, exception);
            }
        }

        Set<Long> seen = new HashSet<>();
        seen.add(imageId);
        List<Long> result = new ArrayList<>();
        if (vector != null) {
            List<Long> vectorResults = milvusService.searchSimilar(
                    vector,
                    Math.max(resultCount * 2, 16),
                    null
            );
            for (Long similarId : vectorResults) {
                if (similarId != null && seen.add(similarId)) {
                    result.add(similarId);
                }
            }
        }

        if (result.size() < resultCount) {
            List<Long> fallback = jdbcTemplate.queryForList(
                    "SELECT id FROM image WHERE status = 1 AND id <> ? "
                            + "ORDER BY CASE WHEN category_id = ? THEN 0 ELSE 1 END, id DESC "
                            + "LIMIT ?",
                    Long.class,
                    imageId,
                    categoryId,
                    resultCount * 2
            );
            for (Long fallbackId : fallback) {
                if (fallbackId != null && seen.add(fallbackId)) {
                    result.add(fallbackId);
                }
                if (result.size() >= resultCount) {
                    break;
                }
            }
        }

        List<Long> finalResult = result.stream()
                .limit(resultCount)
                .collect(Collectors.toList());
        cacheRecommendations(cacheKey, finalResult);
        return Result.success(finalResult);
    }
    private List<Long> buildMixedRecommendations(Long userId, int count) {
        List<UserBehavior> behaviors = loadUserBehaviors(userId);
        List<Map<String, Object>> images = loadImageStatistics();
        if (behaviors.size() < MIN_BEHAVIOR_COUNT) {
            return hotRecommender.recommend(count, images);
        }

        List<Long> recommendations = mixedRecommender.recommend(
                userId,
                count,
                buildUserItemMatrix(),
                images
        );
        return recommendations.isEmpty()
                ? hotRecommender.recommend(count, images)
                : recommendations;
    }

    private int extractAndStoreFeature(Long imageId) {
        String url = jdbcTemplate.queryForObject(
                "SELECT url FROM image WHERE id = ?",
                String.class,
                imageId
        );
        Long categoryId = null;
        try {
            categoryId = jdbcTemplate.queryForObject(
                    "SELECT category_id FROM image WHERE id = ?",
                    Long.class,
                    imageId
            );
        } catch (Exception ignored) {
            // Category is optional for Milvus filtering.
        }

        float[] vector = featureExtractService.extractFeature(url);
        if (vector == null) {
            return 0;
        }
        milvusService.insertVector(imageId, vector, categoryId);
        Result<Void> result = imageFeignClient.updateFeatureExtracted(imageId);
        if (result == null || !Integer.valueOf(200).equals(result.getCode())) {
            throw new IllegalStateException("Failed to update feature extraction status");
        }
        return vector.length;
    }

    private List<UserBehavior> loadUserBehaviors(Long userId) {
        return userBehaviorMapper.selectList(new LambdaQueryWrapper<UserBehavior>()
                .eq(UserBehavior::getUserId, userId)
                .orderByDesc(UserBehavior::getCreateTime));
    }

    private Map<Long, Map<Long, Double>> buildUserItemMatrix() {
        List<UserBehavior> behaviors = userBehaviorMapper.selectList(
                new LambdaQueryWrapper<UserBehavior>()
                        .orderByDesc(UserBehavior::getCreateTime)
        );
        Map<Long, Map<Long, Double>> matrix = new HashMap<>();
        for (UserBehavior behavior : behaviors) {
            if (behavior.getUserId() == null || behavior.getImageId() == null) {
                continue;
            }
            Map<Long, Double> itemRatings = matrix.computeIfAbsent(
                    behavior.getUserId(),
                    key -> new HashMap<>()
            );
            itemRatings.merge(
                    behavior.getImageId(),
                    behaviorScore(behavior.getBehaviorType()),
                    Double::sum
            );
        }
        return matrix;
    }

    private double behaviorScore(Integer behaviorType) {
        if (behaviorType == null) {
            return 1.0D;
        }
        switch (behaviorType) {
            case 2:
                return 3.0D;
            case 3:
                return 5.0D;
            case 5:
                return 4.0D;
            case 6:
                return 4.5D;
            default:
                return 1.0D;
        }
    }

    private List<Map<String, Object>> loadImageStatistics() {
        return jdbcTemplate.queryForList(IMAGE_STATISTICS_SQL);
    }

    private Map<String, Object> loadImageMetadata(Long imageId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT i.id, i.title, i.description, i.tags, "
                        + "i.category_id AS categoryId, c.name AS categoryName "
                        + "FROM image i LEFT JOIN category c ON c.id = i.category_id "
                        + "WHERE i.id = ? AND i.status = 1",
                imageId
        );
        return rows.isEmpty() ? null : new LinkedHashMap<>(rows.get(0));
    }

    private Long loadImageCategoryId(Long imageId) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT category_id FROM image WHERE id = ?",
                    Long.class,
                    imageId
            );
        } catch (Exception ignored) {
            return null;
        }
    }

    private String loadCategoryName(Long categoryId) {
        if (categoryId == null) {
            return "图片";
        }
        try {
            String name = jdbcTemplate.queryForObject(
                    "SELECT name FROM category WHERE id = ?",
                    String.class,
                    categoryId
            );
            return name == null ? "图片" : name;
        } catch (Exception ignored) {
            return "图片";
        }
    }

    private Map<Long, String> loadCategories() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, name FROM category WHERE status = 1 ORDER BY sort, id"
        );
        Map<Long, String> categories = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            Long id = longValue(row.get("id"));
            String name = stringValue(row.get("name"));
            if (id != null && !name.isEmpty()) {
                categories.put(id, name);
            }
        }
        return categories;
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
    private List<Long> getCachedRecommendations(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        if (!(value instanceof List)) {
            return null;
        }

        List<Long> result = new ArrayList<>();
        for (Object item : (List<?>) value) {
            Long imageId = longValue(item);
            if (imageId != null) {
                result.add(imageId);
            }
        }
        return result;
    }

    private void cacheRecommendations(String key, List<Long> recommendations) {
        long cacheHours = Math.max(1L, recommendProperties.getCacheHours());
        redisTemplate.opsForValue().set(
                key,
                new ArrayList<>(recommendations),
                cacheHours,
                TimeUnit.HOURS
        );
    }

    private void persistRecommendationResult(Long userId, List<Long> recommendations) {
        recommendResultMapper.delete(new LambdaQueryWrapper<RecommendResult>()
                .eq(RecommendResult::getUserId, userId));

        RecommendResult result = new RecommendResult();
        result.setUserId(userId);
        result.setImageIds(recommendations.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",")));
        result.setAlgorithmType("mixed");
        result.setCreateTime(LocalDateTime.now());
        recommendResultMapper.insert(result);
    }

    private String mixedCacheKey(Long userId) {
        return CACHE_PREFIX + userId;
    }

    private String algorithmCacheKey(String algorithm, Long userId) {
        return CACHE_PREFIX + algorithm + ":" + userId;
    }

    private boolean isValidBehaviorType(Integer behaviorType) {
        return behaviorType != null
                && (behaviorType == 1
                || behaviorType == 2
                || behaviorType == 3
                || behaviorType == 5
                || behaviorType == 6);
    }

    private int normalizeCount(Integer requested, int fallback) {
        int count = requested == null || requested < 1 ? fallback : requested;
        if (count < 1) {
            count = DEFAULT_RECOMMEND_COUNT;
        }
        return Math.min(count, MAX_RECOMMEND_COUNT);
    }

    private Long longValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value != null) {
            try {
                return Long.valueOf(String.valueOf(value));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
