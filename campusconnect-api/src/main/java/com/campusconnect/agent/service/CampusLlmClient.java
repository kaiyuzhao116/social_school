package com.campusconnect.agent.service;

import com.campusconnect.agent.config.AiLlmProperties;
import com.campusconnect.agent.dto.CampusAgentResponse;
import com.campusconnect.agent.dto.CampusRagChunkDTO;
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
public class CampusLlmClient {

    private final AiLlmProperties aiLlmProperties;
    private final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public CampusAgentResponse generateAnswer(String question, List<CampusRagChunkDTO> chunks) {
        if (aiLlmProperties.getEnabled() == null || !aiLlmProperties.getEnabled()) {
            throw new RuntimeException("AI 未开启");
        }

        if (isBlank(aiLlmProperties.getBaseUrl())
                || isBlank(aiLlmProperties.getApiKey())
                || isBlank(aiLlmProperties.getModel())) {
            throw new RuntimeException("AI 配置不完整");
        }

        try {
            String context = buildContext(chunks);

            String systemPrompt = """
                    你是渤海大学校园事务 Agent。
                    你的任务是根据检索到的学校资料，回答学生关于通知、校历、办事流程、材料清单、截止时间的问题。

                    要求：
                    1. 只能根据【资料】回答，不要编造学校政策。
                    2. 如果资料不足，要明确说“当前知识库资料不足，建议查看原通知或咨询学院/教务处”。
                    3. 回答要用学生能看懂的话，不要太官方。
                    4. 如果能拆解成待办，请生成 todos。
                    5. 如果资料里有截止时间，请生成 deadlines。
                    6. 只返回 JSON，不要返回 Markdown，不要解释。

                    JSON 格式：
                    {
                      "answer": "回答内容",
                      "intent": "SERVICE_GUIDE / NOTICE_SUMMARY / DEADLINE_QUERY / RELATED_CHECK / GENERAL_QA",
                      "confidence": 0.9,
                      "todos": [
                        {"title": "待办事项", "done": false}
                      ],
                      "deadlines": [
                        {"title": "截止事项", "deadline": "时间文本", "importance": "高/中/低"}
                      ]
                    }
                    """;

            String userPrompt = """
                    【用户问题】
                    %s

                    【资料】
                    %s
                    """.formatted(question, context);

            Map<String, Object> requestBody = Map.of(
                    "model", aiLlmProperties.getModel(),
                    "temperature", 0.2,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userPrompt)
                    )
            );

            String body = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(aiLlmProperties.getBaseUrl()))
                    .timeout(Duration.ofSeconds(40))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + aiLlmProperties.getApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("校园 Agent 调用失败，状态码："
                        + response.statusCode()
                        + "，返回："
                        + response.body());
            }

            JsonNode root = objectMapper.readTree(response.body());

            String aiContent = root.path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();

            String json = extractJson(aiContent);

            return objectMapper.readValue(json, CampusAgentResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("校园 Agent 生成回答失败：" + e.getMessage(), e);
        }
    }

    private String buildContext(List<CampusRagChunkDTO> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return "当前没有检索到相关资料。";
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < chunks.size(); i++) {
            CampusRagChunkDTO chunk = chunks.get(i);

            sb.append("资料").append(i + 1).append("：\n");
            sb.append("标题：").append(chunk.getTitle()).append("\n");
            sb.append("来源：").append(chunk.getSourceName()).append("\n");
            sb.append("类型：").append(chunk.getSourceType()).append("\n");
            sb.append("可信度：").append(chunk.getTrustLevel()).append("\n");
            sb.append("内容：").append(chunk.getContent()).append("\n\n");
        }

        return sb.toString();
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