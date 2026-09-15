package com.cr.recommend.service.impl;

import com.cr.common.dto.ImagePredictionDTO;
import com.cr.recommend.config.QwenProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class QwenLLMServiceTest {

    private QwenProperties properties;

    private RestTemplate restTemplate;

    private MockRestServiceServer server;

    private QwenLLMService service;

    @BeforeEach
    void setUp() {
        properties = new QwenProperties();
        properties.setApiKey("test-api-key");
        properties.setBaseUrl("https://dashscope.test/v1/chat/completions");
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        service = new QwenLLMService(properties, new ObjectMapper(), restTemplate);
    }

    @Test
    void shouldParseStructuredMetadataAndSendImageWithRagContext() {
        server.expect(once(), requestTo(properties.getBaseUrl()))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer test-api-key"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "data:image/png;base64,AQID")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "参考人物肖像")))
                .andRespond(withSuccess(
                        "{\"choices\":[{\"message\":{\"content\":"
                                + "\"```json\\n{\\\"title\\\":\\\"证件照人物\\\","
                                + "\\\"description\\\":\\\"白色背景前的人物正面肖像\\\","
                                + "\\\"categoryId\\\":2,\\\"categoryName\\\":\\\"人物\\\","
                                + "\\\"tags\\\":[\\\"人物\\\",\\\"肖像\\\",\\\"证件照\\\"]}\\n```\"}}]}",
                        MediaType.APPLICATION_JSON
                ));

        ImagePredictionDTO result = service.analyzeImage(
                new byte[]{1, 2, 3},
                "image/png",
                ragContexts(),
                categories()
        );

        assertThat(result.getCategoryId()).isEqualTo(2L);
        assertThat(result.getCategoryName()).isEqualTo("人物");
        assertThat(result.getTitleSuggestion()).isEqualTo("证件照人物");
        assertThat(result.getTagsSuggestion()).isEqualTo("人物,肖像,证件照");
        server.verify();
    }

    @Test
    void shouldUseOtherCategoryWhenModelReturnsUnknownCategory() {
        server.expect(once(), requestTo(properties.getBaseUrl()))
                .andRespond(withSuccess(
                        "{\"choices\":[{\"message\":{\"content\":"
                                + "\"{\\\"title\\\":\\\"未知物体\\\","
                                + "\\\"description\\\":\\\"画面主体暂时无法明确归类\\\","
                                + "\\\"categoryId\\\":999,\\\"categoryName\\\":\\\"未知\\\","
                                + "\\\"tags\\\":[\\\"物体\\\"]}\"}}]}",
                        MediaType.APPLICATION_JSON
                ));

        ImagePredictionDTO result = service.analyzeImage(
                new byte[]{1},
                "image/jpeg",
                new ArrayList<Map<String, Object>>(),
                categories()
        );

        assertThat(result.getCategoryId()).isEqualTo(8L);
        assertThat(result.getCategoryName()).isEqualTo("其他");
    }

    @Test
    void shouldRejectMissingApiKeyBeforeSendingRequest() {
        properties.setApiKey(" ");

        assertThatThrownBy(() -> service.analyzeImage(
                new byte[]{1},
                "image/jpeg",
                new ArrayList<Map<String, Object>>(),
                categories()
        )).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("API Key");
    }

    private List<Map<String, Object>> ragContexts() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("id", 10L);
        context.put("title", "参考人物肖像");
        context.put("categoryId", 2L);
        return Arrays.asList(context);
    }

    private Map<Long, String> categories() {
        Map<Long, String> categories = new LinkedHashMap<>();
        categories.put(1L, "风景");
        categories.put(2L, "人物");
        categories.put(8L, "其他");
        return categories;
    }
}
