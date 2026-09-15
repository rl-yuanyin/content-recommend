package com.cr.common.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Common Redis operations based on {@link StringRedisTemplate}.
 */
@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * Stores a value without expiration.
     *
     * @param key Redis key
     * @param value Redis value
     */
    public void set(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    /**
     * Stores a value with an expiration time.
     *
     * @param key Redis key
     * @param value Redis value
     * @param timeout expiration timeout
     * @param unit timeout unit
     */
    public void set(String key, String value, long timeout, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * Gets a value by key.
     *
     * @param key Redis key
     * @return stored value or null
     */
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * Deletes a key.
     *
     * @param key Redis key
     * @return true when the key was deleted
     */
    public Boolean del(String key) {
        return stringRedisTemplate.delete(key);
    }

    /**
     * Sets an expiration time on a key.
     *
     * @param key Redis key
     * @param timeout expiration timeout
     * @param unit timeout unit
     * @return true when the expiration was set
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return stringRedisTemplate.expire(key, timeout, unit);
    }

    /**
     * Stores a value only when the key does not exist.
     *
     * @param key Redis key
     * @param value Redis value
     * @param timeout expiration timeout
     * @param unit timeout unit
     * @return true when the value was stored
     */
    public boolean setnx(String key, String value, long timeout, TimeUnit unit) {
        Boolean result = stringRedisTemplate.opsForValue()
                .setIfAbsent(key, value, timeout, unit);
        return Boolean.TRUE.equals(result);
    }
}
