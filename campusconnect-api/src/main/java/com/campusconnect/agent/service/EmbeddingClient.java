package com.campusconnect.agent.service;

import com.campusconnect.agent.config.AiEmbeddingProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmbeddingClient {

    private final AiEmbeddingProperties aiEmbeddingProperties;
    private final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public List<Double> embed(String text) {
        if (aiEmbeddingProperties.getEnabled() == null || !aiEmbeddingProperties.getEnabled()) {
            throw new RuntimeException("Embedding 未开启");
        }

        if (isBlank(aiEmbeddingProperties.getBaseUrl())
                || isBlank(aiEmbeddingProperties.getApiKey())
                || isBlank(aiEmbeddingProperties.getModel())) {
            throw new RuntimeException("Embedding 配置不完整");
        }

        if (isBlank(text)) {
            throw new RuntimeException("Embedding 文本不能为空");
        }

        try {
            Map<String, Object> requestBody = Map.of(
                    "model", aiEmbeddingProperties.getModel(),
                    "input", text
            );

            String body = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(aiEmbeddingProperties.getBaseUrl()))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + aiEmbeddingProperties.getApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("Embedding 调用失败，状态码："
                        + response.statusCode()
                        + "，返回："
                        + response.body());
            }

            JsonNode root = objectMapper.readTree(response.body());

            JsonNode data = root.path("data");
            if (!data.isArray() || data.isEmpty()) {
                throw new RuntimeException("Embedding 返回中没有 data：" + response.body());
            }

            JsonNode embeddingNode = data.get(0).path("embedding");
            if (!embeddingNode.isArray()) {
                throw new RuntimeException("Embedding 返回中没有 embedding：" + response.body());
            }

            return objectMapper.convertValue(
                    embeddingNode,
                    new TypeReference<List<Double>>() {}
            );
        } catch (Exception e) {
            throw new RuntimeException("生成文本向量失败：" + e.getMessage(), e);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}