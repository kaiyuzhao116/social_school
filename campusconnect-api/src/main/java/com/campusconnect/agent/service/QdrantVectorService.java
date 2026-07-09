package com.campusconnect.agent.service;

import com.campusconnect.agent.config.QdrantProperties;
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
import java.util.*;

@Service
@RequiredArgsConstructor
public class QdrantVectorService {

    private final QdrantProperties qdrantProperties;
    private final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * 确保 collection 存在，不存在就创建
     */
    public void ensureCollection(int vectorSize) {
        if (collectionExists()) {
            return;
        }

        try {
            Map<String, Object> vectors = new HashMap<>();
            vectors.put("size", vectorSize);
            vectors.put("distance", "Cosine");

            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("vectors", vectors);

            String body = objectMapper.writeValueAsString(bodyMap);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(qdrantProperties.getUrl()
                            + "/collections/"
                            + qdrantProperties.getCollectionName()))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("创建 Qdrant Collection 失败，状态码："
                        + response.statusCode()
                        + "，返回："
                        + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("创建 Qdrant Collection 失败：" + e.getMessage(), e);
        }
    }

    /**
     * 插入或更新一个向量点
     */
    public void upsertPoint(
            Long pointId,
            List<Double> vector,
            Map<String, Object> payload
    ) {
        try {
            Map<String, Object> point = new HashMap<>();
            point.put("id", pointId);
            point.put("vector", vector);
            point.put("payload", payload);

            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("points", List.of(point));

            String body = objectMapper.writeValueAsString(bodyMap);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(qdrantProperties.getUrl()
                            + "/collections/"
                            + qdrantProperties.getCollectionName()
                            + "/points?wait=true"))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("写入 Qdrant 失败，状态码："
                        + response.statusCode()
                        + "，返回："
                        + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("写入 Qdrant 向量失败：" + e.getMessage(), e);
        }
    }

    /**
     * 搜索相似向量
     */
    public List<Map<String, Object>> search(List<Double> vector, int limit) {
        try {
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("vector", vector);
            bodyMap.put("limit", limit);
            bodyMap.put("with_payload", true);
            bodyMap.put("with_vector", false);

            String body = objectMapper.writeValueAsString(bodyMap);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(qdrantProperties.getUrl()
                            + "/collections/"
                            + qdrantProperties.getCollectionName()
                            + "/points/search"))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("搜索 Qdrant 失败，状态码："
                        + response.statusCode()
                        + "，返回："
                        + response.body());
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode result = root.path("result");

            List<Map<String, Object>> list = new ArrayList<>();

            if (result.isArray()) {
                for (JsonNode item : result) {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", item.path("id").asText());
                    map.put("score", item.path("score").asDouble());

                    Map<String, Object> payload = objectMapper.convertValue(
                            item.path("payload"),
                            new TypeReference<Map<String, Object>>() {}
                    );

                    map.put("payload", payload);
                    list.add(map);
                }
            }

            return list;
        } catch (Exception e) {
            throw new RuntimeException("Qdrant 搜索失败：" + e.getMessage(), e);
        }
    }

    private boolean collectionExists() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(qdrantProperties.getUrl()
                            + "/collections/"
                            + qdrantProperties.getCollectionName()))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
    public void deleteCollection() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(qdrantProperties.getUrl()
                            + "/collections/"
                            + qdrantProperties.getCollectionName()))
                    .timeout(Duration.ofSeconds(30))
                    .DELETE()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            // collection 不存在时，Qdrant 可能返回 404，这里不算严重错误
            if (response.statusCode() == 404) {
                return;
            }

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("删除 Qdrant Collection 失败，状态码："
                        + response.statusCode()
                        + "，返回："
                        + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("删除 Qdrant Collection 失败：" + e.getMessage(), e);
        }
    }
}