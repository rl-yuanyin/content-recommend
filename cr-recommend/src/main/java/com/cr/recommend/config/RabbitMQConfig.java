package com.cr.recommend.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ topology for image feature extraction.
 */
@Configuration
public class RabbitMQConfig {

    public static final String CONTENT_EXCHANGE = "content.exchange";

    public static final String IMAGE_FEATURE_EXTRACT_QUEUE = "image.feature.extract.queue";

    public static final String IMAGE_FEATURE_EXTRACT_ROUTING_KEY = "image.feature.extract";

    /**
     * Main image event exchange.
     *
     * @return durable topic exchange
     */
    @Bean
    public TopicExchange contentExchange() {
        return new TopicExchange(CONTENT_EXCHANGE, true, false);
    }

    /**
     * Queue consumed by the image feature extraction listener.
     *
     * @return durable queue
     */
    @Bean
    public Queue imageFeatureExtractQueue() {
        return QueueBuilder.durable(IMAGE_FEATURE_EXTRACT_QUEUE).build();
    }

    /**
     * Binds the feature extraction queue.
     *
     * @return queue binding
     */
    @Bean
    public Binding imageFeatureExtractBinding() {
        return BindingBuilder.bind(imageFeatureExtractQueue())
                .to(contentExchange())
                .with(IMAGE_FEATURE_EXTRACT_ROUTING_KEY);
    }

    /**
     * Uses JSON payloads for image events.
     *
     * @return JSON message converter
     */
    @Bean
    public MessageConverter rabbitMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
