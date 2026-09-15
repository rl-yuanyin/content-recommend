package com.cr.recommend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration for DashScope's OpenAI-compatible Qwen-VL endpoint.
 */
@Data
@Component
@ConfigurationProperties(prefix = "qwen")
public class QwenProperties {

    private String apiKey;

    private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

    private String model = "qwen-vl-plus";

    private int connectTimeoutMs = 10000;

    private int readTimeoutMs = 60000;

    private long maxImageBytes = 10485760L;
}
