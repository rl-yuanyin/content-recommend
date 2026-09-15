package com.cr.recommend.config;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Milvus vector database configuration.
 */
@Configuration
public class MilvusConfig {

    @Value("${milvus.host:localhost}")
    private String host;

    @Value("${milvus.port:19530}")
    private int port;

    @Value("${milvus.username:root}")
    private String username;

    @Value("${milvus.password:Milvus}")
    private String password;

    /**
     * Creates a Milvus 2.x client.
     *
     * @return Milvus client
     */
    @Bean(destroyMethod = "")
    public MilvusClientV2 milvusClientV2() {
        ConnectConfig config = ConnectConfig.builder()
                .uri("http://" + host + ":" + port)
                .username(username)
                .password(password)
                .build();
        return new MilvusClientV2(config);
    }
}
