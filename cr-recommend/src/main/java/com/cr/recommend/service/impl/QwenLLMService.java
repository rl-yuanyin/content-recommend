package com.cr.recommend.service.impl;

import com.cr.common.dto.ImagePredictionDTO;
import com.cr.recommend.config.QwenProperties;
import com.cr.recommend.service.LLMService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Qwen-VL implementation backed by DashScope's OpenAI-compatible API.
 */
@Service
public class QwenLLMService implements LLMService {

    private static final int MAX_TITLE_LENGTH = 200;

    private static final int MAX_DESCRIPTION_LENGTH = 500;

    private static final int MAX_TAGS_LENGTH = 200;

    private final QwenProperties properties;

    private final ObjectMapper objectMapper;

    private final RestTemplate restTemplate;

    @Autowired
    public QwenLLMService(
            QwenProperties properties,
            ObjectMapper objectMapper,
            RestTemplateBuilder restTemplateBuilder) {
        this(
                properties,
                objectMapper,
                restTemplateBuilder
                        .setConnectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()))
                        .setReadTimeout(Duration.ofMillis(properties.getReadTimeoutMs()))
                        .build()
        );
    }

    QwenLLMService(
            QwenProperties properties,
            ObjectMapper objectMapper,
            RestTemplate restTemplate) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }

    @Override
    public ImagePredictionDTO analyzeImage(
            byte[] imageBytes,
            String contentType,
            List<Map<String, Object>> ragContexts,
            Map<Long, String> categories) {
        validateRequest(imageBytes, categories);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(properties.getApiKey().trim());

        Map<String, Object> request = buildRequest(
                imageBytes,
                normalizeContentType(contentType),
                ragContexts,
                categories
        );
        try {
            String response = restTemplate.postForObject(
                    properties.getBaseUrl(),
                    new HttpEntity<>(request, headers),
                    String.class
            );
            return parseResponse(response, categories);
        } catch (RestClientException exception) {
            throw new IllegalStateException("通义千问VL调用失败", exception);
        }
    }

    private void validateRequest(byte[] imageBytes, Map<Long, String> categories) {
        if (properties.getApiKey() == null || properties.getApiKey().trim().isEmpty()) {
            throw new IllegalStateException("未配置通义千问API Key");
        }
        if (imageBytes == null || imageBytes.length == 0) {
            throw new IllegalArgumentException("图片内容不能为空");
        }
        if (imageBytes.length > properties.getMaxImageBytes()) {
            throw new IllegalArgumentException("图片超过通义千问接口大小限制");
        }
        if (categories == null || categories.isEmpty()) {
            throw new IllegalStateException("图片分类数据为空");
        }
    }

    private Map<String, Object> buildRequest(
            byte[] imageBytes,
            String contentType,
            List<Map<String, Object>> ragContexts,
            Map<Long, String> categories) {
        Map<String, Object> imageUrl = new LinkedHashMap<>();
        imageUrl.put(
                "url",
                "data:" + contentType + ";base64," + Base64.getEncoder().encodeToString(imageBytes)
        );

        Map<String, Object> imagePart = new LinkedHashMap<>();
        imagePart.put("type", "image_url");
        imagePart.put("image_url", imageUrl);

        Map<String, Object> textPart = new LinkedHashMap<>();
        textPart.put("type", "text");
        textPart.put("text", buildPrompt(ragContexts, categories));

        List<Map<String, Object>> content = new ArrayList<>();
        content.add(imagePart);
        content.add(textPart);

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("role", "user");
        message.put("content", content);

        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(message);

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("model", properties.getModel());
        request.put("messages", messages);
        request.put("temperature", 0.1D);
        request.put("max_tokens", 600);
        return request;
    }

    private String buildPrompt(
            List<Map<String, Object>> ragContexts,
            Map<Long, String> categories) {
        try {
            return "你是图片内容审核与标注专家。请以图片本身的视觉证据为主要依据，"
                    + "生成准确、简洁的中文元数据。以下相似图片由ResNet50和Milvus检索得到，"
                    + "只可作为RAG参考，不得因为相似图片标题而忽略图片中的人物、证件、文字或主体。\n"
                    + "允许的分类(ID:名称)：" + objectMapper.writeValueAsString(categories) + "\n"
                    + "相似图片参考：" + objectMapper.writeValueAsString(ragContexts) + "\n"
                    + "只输出一个JSON对象，不要Markdown代码块或解释。格式必须为："
                    + "{\"title\":\"不超过20字的客观标题\","
                    + "\"description\":\"不超过120字的客观描述\","
                    + "\"categoryId\":1,\"categoryName\":\"分类名\","
                    + "\"tags\":[\"标签1\",\"标签2\",\"标签3\"]}。"
                    + "categoryId和categoryName必须来自允许分类；无法归类时使用其他。"
                    + "不要猜测真实姓名、证件号码等敏感身份信息。";
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("构建视觉理解提示词失败", exception);
        }
    }

    private ImagePredictionDTO parseResponse(
            String response,
            Map<Long, String> categories) {
        if (response == null || response.trim().isEmpty()) {
            throw new IllegalStateException("通义千问VL返回内容为空");
        }
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            String modelOutput = content.isTextual() ? content.asText() : content.toString();
            JsonNode result = objectMapper.readTree(extractJsonObject(modelOutput));

            Long categoryId = resolveCategoryId(result, categories);
            String title = requiredText(result, "title");
            String description = requiredText(result, "description");
            String tags = parseTags(result.path("tags"));

            ImagePredictionDTO prediction = new ImagePredictionDTO();
            prediction.setCategoryId(categoryId);
            prediction.setCategoryName(categories.get(categoryId));
            prediction.setTitleSuggestion(truncate(title, MAX_TITLE_LENGTH));
            prediction.setDescriptionSuggestion(truncate(description, MAX_DESCRIPTION_LENGTH));
            prediction.setTagsSuggestion(truncate(tags, MAX_TAGS_LENGTH));
            return prediction;
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("通义千问VL返回内容无法解析", exception);
        }
    }

    private Long resolveCategoryId(JsonNode result, Map<Long, String> categories) {
        JsonNode categoryIdNode = result.path("categoryId");
        if (categoryIdNode.canConvertToLong()) {
            long categoryId = categoryIdNode.asLong();
            if (categories.containsKey(categoryId)) {
                return categoryId;
            }
        }

        String requestedName = result.path("categoryName").asText("").trim();
        for (Map.Entry<Long, String> category : categories.entrySet()) {
            if (category.getValue().equalsIgnoreCase(requestedName)) {
                return category.getKey();
            }
        }
        for (Map.Entry<Long, String> category : categories.entrySet()) {
            if ("其他".equals(category.getValue())) {
                return category.getKey();
            }
        }
        throw new IllegalStateException("通义千问VL返回了无效分类");
    }

    private String requiredText(JsonNode result, String fieldName) {
        String value = result.path(fieldName).asText("").trim();
        if (value.isEmpty()) {
            throw new IllegalStateException("通义千问VL未返回" + fieldName);
        }
        return value;
    }

    private String parseTags(JsonNode tagsNode) {
        List<String> tags = new ArrayList<>();
        if (tagsNode.isArray()) {
            for (JsonNode tag : tagsNode) {
                addTag(tags, tag.asText(""));
            }
        } else {
            String[] values = tagsNode.asText("").split("[,，]");
            for (String value : values) {
                addTag(tags, value);
            }
        }
        if (tags.isEmpty()) {
            throw new IllegalStateException("通义千问VL未返回有效标签");
        }
        return String.join(",", tags);
    }

    private void addTag(List<String> tags, String value) {
        String normalized = value == null ? "" : value.trim();
        if (!normalized.isEmpty() && !tags.contains(normalized) && tags.size() < 8) {
            tags.add(normalized);
        }
    }

    private String extractJsonObject(String modelOutput) {
        int start = modelOutput == null ? -1 : modelOutput.indexOf('{');
        int end = modelOutput == null ? -1 : modelOutput.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw new IllegalStateException("通义千问VL未返回JSON对象");
        }
        return modelOutput.substring(start, end + 1);
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            return MediaType.IMAGE_JPEG_VALUE;
        }
        return "image/jpg".equalsIgnoreCase(contentType)
                ? MediaType.IMAGE_JPEG_VALUE
                : contentType.toLowerCase(Locale.ROOT);
    }

    private String truncate(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
