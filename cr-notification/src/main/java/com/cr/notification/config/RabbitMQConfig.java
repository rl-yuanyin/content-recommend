package com.cr.notification.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ topology for notification messages.
 */
@Configuration
public class RabbitMQConfig {

    public static final String CONTENT_EXCHANGE = "content.exchange";

    public static final String LIKE_QUEUE = "notification.like.queue";

    public static final String COMMENT_QUEUE = "notification.comment.queue";

    public static final String COLLECT_QUEUE = "notification.collect.queue";

    public static final String SYSTEM_QUEUE = "notification.system.queue";

    public static final String LIKE_ROUTING_KEY = "notification.like";

    public static final String COMMENT_ROUTING_KEY = "notification.comment";

    public static final String COLLECT_ROUTING_KEY = "notification.collect";

    public static final String SYSTEM_ROUTING_KEY = "notification.system";

    public static final String DEAD_LETTER_EXCHANGE = "notification.dlx";

    public static final String DEAD_LETTER_QUEUE = "notification.dead.queue";

    public static final String DEAD_LETTER_ROUTING_KEY = "notification.dead";

    /**
     * Main notification topic exchange.
     *
     * @return topic exchange
     */
    @Bean
    public TopicExchange contentExchange() {
        return new TopicExchange(CONTENT_EXCHANGE, true, false);
    }

    /**
     * Dead-letter exchange.
     *
     * @return dead-letter exchange
     */
    @Bean
    public DirectExchange notificationDeadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE, true, false);
    }

    /**
     * Like notification queue.
     *
     * @return durable queue with dead-letter routing
     */
    @Bean
    public Queue likeNotificationQueue() {
        return buildQueue(LIKE_QUEUE);
    }

    /**
     * Comment notification queue.
     *
     * @return durable queue with dead-letter routing
     */
    @Bean
    public Queue commentNotificationQueue() {
        return buildQueue(COMMENT_QUEUE);
    }

    /**
     * Collection notification queue.
     *
     * @return durable queue with dead-letter routing
     */
    @Bean
    public Queue collectNotificationQueue() {
        return buildQueue(COLLECT_QUEUE);
    }

    /**
     * System notification queue.
     *
     * @return durable queue with dead-letter routing
     */
    @Bean
    public Queue systemNotificationQueue() {
        return buildQueue(SYSTEM_QUEUE);
    }

    /**
     * Dead-letter queue.
     *
     * @return durable dead-letter queue
     */
    @Bean
    public Queue notificationDeadLetterQueue() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
    }

    /**
     * Binds the like queue.
     *
     * @return binding
     */
    @Bean
    public Binding likeNotificationBinding() {
        return BindingBuilder.bind(likeNotificationQueue())
                .to(contentExchange())
                .with(LIKE_ROUTING_KEY);
    }

    /**
     * Binds the comment queue.
     *
     * @return binding
     */
    @Bean
    public Binding commentNotificationBinding() {
        return BindingBuilder.bind(commentNotificationQueue())
                .to(contentExchange())
                .with(COMMENT_ROUTING_KEY);
    }

    /**
     * Binds the collection queue.
     *
     * @return binding
     */
    @Bean
    public Binding collectNotificationBinding() {
        return BindingBuilder.bind(collectNotificationQueue())
                .to(contentExchange())
                .with(COLLECT_ROUTING_KEY);
    }

    /**
     * Binds the system queue.
     *
     * @return binding
     */
    @Bean
    public Binding systemNotificationBinding() {
        return BindingBuilder.bind(systemNotificationQueue())
                .to(contentExchange())
                .with(SYSTEM_ROUTING_KEY);
    }

    /**
     * Binds the dead-letter queue.
     *
     * @return binding
     */
    @Bean
    public Binding notificationDeadLetterBinding() {
        return BindingBuilder.bind(notificationDeadLetterQueue())
                .to(notificationDeadLetterExchange())
                .with(DEAD_LETTER_ROUTING_KEY);
    }

    /**
     * Uses JSON payloads for notification messages.
     *
     * @return JSON message converter
     */
    @Bean
    public MessageConverter rabbitMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    private Queue buildQueue(String queueName) {
        return QueueBuilder.durable(queueName)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(DEAD_LETTER_ROUTING_KEY)
                .build();
    }
}
