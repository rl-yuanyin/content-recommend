package com.cr.notification.listener;

import cn.hutool.core.util.StrUtil;
import com.cr.notification.config.RabbitMQConfig;
import com.cr.notification.constant.NotificationConstants;
import com.cr.notification.entity.Notification;
import com.cr.notification.mapper.NotificationMapper;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * RabbitMQ listener that persists notification messages.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationListener {

    private static final int MAX_RETRY_COUNT = 3;

    private static final long RETRY_CACHE_HOURS = 1L;

    private final NotificationMapper notificationMapper;

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * Handles like notifications.
     *
     * @param message AMQP message
     * @param channel AMQP channel
     * @param payload JSON payload
     * @throws IOException when acknowledgement or rejection fails
     */
    @RabbitListener(queues = RabbitMQConfig.LIKE_QUEUE)
    public void handleLikeMessage(
            Message message,
            Channel channel,
            @Payload Map<String, Object> payload) throws IOException {
        processMessage(
                message,
                channel,
                payload,
                1,
                "收到新的点赞",
                "您的图片收到一个点赞"
        );
    }

    /**
     * Handles comment notifications.
     *
     * @param message AMQP message
     * @param channel AMQP channel
     * @param payload JSON payload
     * @throws IOException when acknowledgement or rejection fails
     */
    @RabbitListener(queues = RabbitMQConfig.COMMENT_QUEUE)
    public void handleCommentMessage(
            Message message,
            Channel channel,
            @Payload Map<String, Object> payload) throws IOException {
        processMessage(
                message,
                channel,
                payload,
                2,
                "收到新的评论",
                "您的图片收到一条新评论"
        );
    }

    /**
     * Handles image collection notifications.
     *
     * @param message AMQP message
     * @param channel AMQP channel
     * @param payload JSON payload
     * @throws IOException when acknowledgement or rejection fails
     */
    @RabbitListener(queues = RabbitMQConfig.COLLECT_QUEUE)
    public void handleCollectMessage(
            Message message,
            Channel channel,
            @Payload Map<String, Object> payload) throws IOException {
        processMessage(
                message,
                channel,
                payload,
                3,
                "收到新的收藏",
                "您的图片被收藏"
        );
    }

    /**
     * Handles system notifications.
     *
     * @param message AMQP message
     * @param channel AMQP channel
     * @param payload JSON payload
     * @throws IOException when acknowledgement or rejection fails
     */
    @RabbitListener(queues = RabbitMQConfig.SYSTEM_QUEUE)
    public void handleSystemMessage(
            Message message,
            Channel channel,
            @Payload Map<String, Object> payload) throws IOException {
        processMessage(
                message,
                channel,
                payload,
                4,
                "系统通知",
                "您有一条新的系统通知"
        );
    }

    /**
     * Persists a notification and acknowledges the message.
     *
     * @param message AMQP message
     * @param channel AMQP channel
     * @param payload message payload
     * @param type notification type
     * @param defaultTitle default title
     * @param defaultContent default content
     * @throws IOException when acknowledgement or rejection fails
     */
    private void processMessage(
            Message message,
            Channel channel,
            Map<String, Object> payload,
            int type,
            String defaultTitle,
            String defaultContent) throws IOException {
        String retryKey = retryKey(message, channel);
        try {
            Notification notification = buildNotification(
                    payload,
                    type,
                    defaultTitle,
                    defaultContent
            );
            if (notificationMapper.insert(notification) != 1) {
                throw new IllegalStateException("Failed to insert notification");
            }

            stringRedisTemplate.delete(retryKey);
            stringRedisTemplate.delete(
                    NotificationConstants.UNREAD_CACHE_PREFIX + notification.getUserId()
            );
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            log.info(
                    "Notification {} saved for user {}",
                    type,
                    notification.getUserId()
            );
        } catch (Exception exception) {
            handleFailure(message, channel, retryKey, exception);
        }
    }

    /**
     * Builds a notification entity from the message body.
     *
     * @param payload message payload
     * @param type notification type
     * @param defaultTitle default title
     * @param defaultContent default content
     * @return notification entity
     */
    private Notification buildNotification(
            Map<String, Object> payload,
            int type,
            String defaultTitle,
            String defaultContent) {
        if (payload == null || payload.isEmpty()) {
            throw new IllegalArgumentException("Notification payload is empty");
        }

        Long userId = longValue(firstValue(
                payload,
                "receiverId",
                "targetUserId",
                "authorId",
                "userId"
        ));
        if (userId == null && type == 4) {
            userId = 0L;
        }
        if (userId == null) {
            throw new IllegalArgumentException("Notification receiver is missing");
        }

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(StrUtil.blankToDefault(
                stringValue(payload.get("title")),
                defaultTitle
        ));
        notification.setContent(StrUtil.blankToDefault(
                stringValue(payload.get("content")),
                defaultContent
        ));
        notification.setRelatedId(longValue(firstValue(
                payload,
                "relatedId",
                "imageId",
                "commentId"
        )));
        notification.setIsRead(NotificationConstants.UNREAD);
        notification.setCreateTime(LocalDateTime.now());
        return notification;
    }

    /**
     * Requeues a failed message up to three times, then routes it to the DLQ.
     *
     * @param message AMQP message
     * @param channel AMQP channel
     * @param retryKey Redis retry key
     * @param exception processing exception
     * @throws IOException when rejection fails
     */
    private void handleFailure(
            Message message,
            Channel channel,
            String retryKey,
            Exception exception) throws IOException {
        Long attempts = stringRedisTemplate.opsForValue().increment(retryKey);
        if (attempts != null && attempts == 1L) {
            stringRedisTemplate.expire(retryKey, RETRY_CACHE_HOURS, TimeUnit.HOURS);
        }

        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        if (attempts == null || attempts <= MAX_RETRY_COUNT) {
            log.warn(
                    "Notification processing failed, requeueing message. attempt={}, deliveryTag={}",
                    attempts,
                    deliveryTag,
                    exception
            );
            channel.basicNack(deliveryTag, false, true);
            return;
        }

        stringRedisTemplate.delete(retryKey);
        channel.basicNack(deliveryTag, false, false);
        log.error(
                "Notification retry limit reached, message sent to dead-letter queue. "
                        + "deliveryTag={}",
                deliveryTag,
                exception
        );
    }

    /**
     * Builds a stable retry key for a message.
     *
     * @param message AMQP message
     * @param channel AMQP channel
     * @return retry key
     */
    private String retryKey(Message message, Channel channel) {
        String messageId = message.getMessageProperties().getMessageId();
        if (StrUtil.isBlank(messageId)) {
            messageId = channel.getChannelNumber()
                    + ":"
                    + message.getMessageProperties().getDeliveryTag();
        }
        return NotificationConstants.RETRY_CACHE_PREFIX + messageId;
    }

    /**
     * Returns the first non-null payload value among the supplied keys.
     *
     * @param payload message payload
     * @param keys candidate keys
     * @return first non-null value or null
     */
    private Object firstValue(Map<String, Object> payload, String... keys) {
        for (String key : keys) {
            Object value = payload.get(key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    /**
     * Safely converts a payload value to Long.
     *
     * @param value input value
     * @return Long value or null
     */
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

    /**
     * Safely converts a payload value to String.
     *
     * @param value input value
     * @return string value or empty string
     */
    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
