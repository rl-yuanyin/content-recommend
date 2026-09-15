package com.cr.common.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Shared constants used across services.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommonConstants {

    /**
     * HTTP header carrying the access token.
     */
    public static final String TOKEN_HEADER = "Authorization";

    /**
     * Bearer token prefix.
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * Redis key prefix for user data.
     */
    public static final String REDIS_USER_PREFIX = "user:";

    /**
     * Redis key prefix for tokens.
     */
    public static final String REDIS_TOKEN_PREFIX = "token:";

    /**
     * Redis key prefix for content data.
     */
    public static final String REDIS_CONTENT_PREFIX = "content:";

    /**
     * Redis key prefix for recommendation data.
     */
    public static final String REDIS_RECOMMEND_PREFIX = "recommend:";

    /**
     * Default UTF-8 encoding.
     */
    public static final String UTF8 = "UTF-8";

    /**
     * Enabled status.
     */
    public static final Integer STATUS_ENABLED = 1;

    /**
     * Disabled status.
     */
    public static final Integer STATUS_DISABLED = 0;
}
