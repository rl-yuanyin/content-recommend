package com.cr.recommend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Recommendation algorithm configuration.
 */
@Data
@Component
@ConfigurationProperties(prefix = "recommend")
public class RecommendProperties {

    /**
     * Number of image-content recommendations.
     */
    private int contentRecommendCount = 10;

    /**
     * Number of collaborative-filtering recommendations.
     */
    private int cfRecommendCount = 10;

    /**
     * Number of hot recommendations.
     */
    private int hotRecommendCount = 10;

    /**
     * Recommendation cache duration in hours.
     */
    private long cacheHours = 2;
}
