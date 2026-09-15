package com.cr.image.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ serialization configuration for image events.
 */
@Configuration
public class RabbitMQConfig {

    /**
     * Sends RabbitMQ payloads as JSON so notification consumers can map them.
     *
     * @return JSON message converter
     */
    @Bean
    public MessageConverter rabbitMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
