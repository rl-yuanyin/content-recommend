package com.cr.recommend.listener;

import cn.hutool.core.util.StrUtil;
import com.cr.common.result.Result;
import com.cr.recommend.config.RabbitMQConfig;
import com.cr.recommend.feign.ImageFeignClient;
import com.cr.recommend.service.FeatureExtractService;
import com.cr.recommend.service.MilvusService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Consumes image upload events and asynchronously extracts deep features.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ImageFeatureExtractListener {

    private static final int MAX_RETRY_COUNT = 3;

    private static final String RETRY_KEY_PREFIX = "recommend:feature:retry:";

    private final FeatureExtractService featureExtractService;

    private final MilvusService milvusService;

    private final ImageFeignClient imageFeignClient;

    private final JdbcTemplate jdbcTemplate;

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * Extracts a feature vector, stores it in Milvus, and updates image status.
     *
     * @param message AMQP message
     * @param channel AMQP channel
     * @param payload image feature event
     * @throws IOException when acknowledgement fails
     */
    @RabbitListener(queues = RabbitMQConfig.IMAGE_FEATURE_EXTRACT_QUEUE)
    public void handleImageFeatureExtract(
            Message message,
            Channel channel,
            @Payload Map<String, Object> payload) throws IOException {
        String retryKey = retryKey(message, channel);
        try {
            Long imageId = longValue(payload.get("imageId"));
            String imageUrl = stringValue(payload.get("imageUrl"));
            if (imageId == null || StrUtil.isBlank(imageUrl)) {
                throw new IllegalArgumentException("imageId and imageUrl are required");
            }

            float[] feature = featureExtractService.extractFeature(imageUrl);
            if (feature == null) {
                throw new IllegalStateException("Feature extraction returned null");
            }

            Long categoryId = loadCategoryId(imageId);
            milvusService.insertVector(imageId, feature, categoryId);
            Result<Void> result = imageFeignClient.updateFeatureExtracted(imageId);
            if (result == null || !Integer.valueOf(200).equals(result.getCode())) {
                throw new IllegalStateException("Failed to update feature extraction status");
            }

            stringRedisTemplate.delete(retryKey);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            log.info("Image {} feature extracted with dimension {}", imageId, feature.length);
        } catch (Exception exception) {
            handleFailure(message, channel, retryKey, exception);
        }
    }

    private Long loadCategoryId(Long imageId) {
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

    private void handleFailure(
            Message message,
            Channel channel,
            String retryKey,
            Exception exception) throws IOException {
        Long attempts = stringRedisTemplate.opsForValue().increment(retryKey);
        if (attempts != null && attempts == 1L) {
            stringRedisTemplate.expire(retryKey, 1L, TimeUnit.HOURS);
        }

        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        if (attempts == null || attempts <= MAX_RETRY_COUNT) {
            log.warn(
                    "Feature extraction failed, requeueing. attempt={}, deliveryTag={}",
                    attempts,
                    deliveryTag,
                    exception
            );
            channel.basicNack(deliveryTag, false, true);
            return;
        }

        stringRedisTemplate.delete(retryKey);
        channel.basicNack(deliveryTag, false, false);
        log.error("Feature extraction retry limit reached. deliveryTag={}", deliveryTag, exception);
    }

    private String retryKey(Message message, Channel channel) {
        String messageId = message.getMessageProperties().getMessageId();
        if (StrUtil.isBlank(messageId)) {
            messageId = channel.getChannelNumber()
                    + ":"
                    + message.getMessageProperties().getDeliveryTag();
        }
        return RETRY_KEY_PREFIX + messageId;
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

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
