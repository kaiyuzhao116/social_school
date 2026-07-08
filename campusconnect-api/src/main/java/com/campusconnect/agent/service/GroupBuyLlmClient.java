package com.campusconnect.agent.service;

import com.campusconnect.agent.config.AiLlmProperties;
import com.campusconnect.agent.dto.GroupBuyDraftDTO;
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
public class GroupBuyLlmClient {

    private final AiLlmProperties aiLlmProperties;
    private final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public GroupBuyDraftDTO generateDraftByLlm(String content) {
        if (aiLlmProperties.getEnabled() == null || !aiLlmProperties.getEnabled()) {
            throw new RuntimeException("AI 未开启");
        }

        if (isBlank(aiLlmProperties.getBaseUrl())
                || isBlank(aiLlmProperties.getApiKey())
                || isBlank(aiLlmProperties.getModel())) {
            throw new RuntimeException("AI 配置不完整");
        }

        try {
            String systemPrompt = """
                    你是校园拼团助手。
                    你的任务是根据用户的一句话，生成校园拼团草稿。

                    要求：
                    1. 只返回 JSON，不要返回 Markdown，不要解释。
                    2. 不要创建拼团，只生成草稿。
                    3. category 只能从以下值中选择：拼车、拼饭、团购、运动、其他。
                    4. 如果用户说“找 N 个人”，targetCount 应该是 N + 1，因为还要算上用户自己。
                    5. 如果用户没说具体人数，targetCount 默认是 2。
                    6. 如果地点不明确，location 返回“校内”。
                    7. startTimeText 保留自然语言时间，例如：今晚、明天下午、周六晚上。
                    8. description 要像正常学生发帖，不要写“根据你的需求生成”这种系统话术。

                    JSON 格式：
                    {
                      "title": "",
                      "category": "",
                      "targetCount": 2,
                      "location": "",
                      "startTimeText": "",
                      "description": ""
                    }
                    """;

            Map<String, Object> requestBody = Map.of(
                    "model", aiLlmProperties.getModel(),
                    "temperature", 0.2,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", content)
                    )
            );

            String body = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(aiLlmProperties.getBaseUrl()))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + aiLlmProperties.getApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("AI 调用失败，状态码：" + response.statusCode() + "，返回：" + response.body());
            }

            JsonNode root = objectMapper.readTree(response.body());

            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                throw new RuntimeException("AI 返回中没有 choices：" + response.body());
            }

            String aiContent = choices.get(0)
                    .path("message")
                    .path("content")
                    .asText();

            String json = extractJson(aiContent);

            return objectMapper.readValue(json, GroupBuyDraftDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("生成拼团草稿失败：" + e.getMessage(), e);
        }
    }

    private String extractJson(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new RuntimeException("AI 返回内容为空");
        }

        String cleaned = text
                .replace("```json", "")
                .replace("```", "")
                .trim();

        int start = cleaned.indexOf("{");
        int end = cleaned.lastIndexOf("}");

        if (start < 0 || end < 0 || end <= start) {
            throw new RuntimeException("AI 返回内容不是 JSON：" + text);
        }

        return cleaned.substring(start, end + 1);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}