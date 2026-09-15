package com.cr.recommend.service;

import com.cr.common.result.Result;

/**
 * Recommendation service.
 */
public interface RecommendService {

    /**
     * Gets mixed recommendations.
     *
     * @param userId user identifier
     * @param count recommendation count
     * @return recommended image identifiers
     */
    Result getRecommend(Long userId, Integer count);

    /**
     * Gets image-content recommendations.
     *
     * @param userId user identifier
     * @param count recommendation count
     * @return recommended image identifiers
     */
    Result getContentBasedRecommend(Long userId, Integer count);

    /**
     * Gets collaborative-filtering recommendations.
     *
     * @param userId user identifier
     * @param count recommendation count
     * @return recommended image identifiers
     */
    Result getCFRecommend(Long userId, Integer count);

    /**
     * Gets hot recommendations.
     *
     * @param count recommendation count
     * @return recommended image identifiers
     */
    Result getHotRecommend(Integer count);

    /**
     * Records a user behavior.
     *
     * @param userId user identifier
     * @param imageId image identifier
     * @param behaviorType behavior type
     * @param duration dwell time in seconds
     * @return recording result
     */
    Result recordBehavior(
            Long userId,
            Long imageId,
            Integer behaviorType,
            Integer duration);

    /**
     * Asynchronously refreshes recommendations for a user.
     *
     * @param userId user identifier
     * @return refresh result
     */
    Result refreshRecommend(Long userId);

    /**
     * Manually extracts and stores a feature vector for an image.
     *
     * @param imageId image identifier
     * @return feature dimension
     */
    Result extractFeature(Long imageId);

    /**
     * Initializes Milvus and imports vectors for existing images.
     *
     * @return imported image count
     */
    Result initMilvus();
    /**
     * Predicts image metadata using multimodal understanding and RAG context.
     *
     * @param file uploaded image file
     * @return prediction result
     */
    Result predictImage(org.springframework.web.multipart.MultipartFile file);

    /**
     * Gets images similar to the specified image.
     *
     * @param imageId image identifier
     * @param count result count
     * @return similar image identifiers
     */
    Result getSimilarImages(Long imageId, Integer count);}
