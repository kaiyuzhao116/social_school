package com.campusconnect.agent.service;

import com.campusconnect.agent.dto.CampusRagChunkDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CampusRagService {

    private final EmbeddingClient embeddingClient;
    private final QdrantVectorService qdrantVectorService;

    public List<CampusRagChunkDTO> retrieve(String question, int limit) {
        if (question == null || question.trim().isEmpty()) {
            throw new RuntimeException("问题不能为空");
        }

        List<Double> queryVector = embeddingClient.embed(question);

        List<Map<String, Object>> searchResults = qdrantVectorService.search(queryVector, limit);

        List<CampusRagChunkDTO> chunks = new ArrayList<>();

        for (Map<String, Object> item : searchResults) {
            Map<String, Object> payload = (Map<String, Object>) item.get("payload");

            CampusRagChunkDTO dto = new CampusRagChunkDTO();
            dto.setScore(toDouble(item.get("score")));

            dto.setChunkId(toLong(payload.get("chunkId")));
            dto.setKnowledgeId(toLong(payload.get("knowledgeId")));
            dto.setTitle(toString(payload.get("title")));
            dto.setSourceName(toString(payload.get("sourceName")));
            dto.setSourceType(toString(payload.get("sourceType")));
            dto.setUrl(toString(payload.get("url")));
            dto.setTrustLevel(toString(payload.get("trustLevel")));
            dto.setContent(toString(payload.get("content")));

            chunks.add(dto);
        }

        return chunks;
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {
            return number.longValue();
        }

        return Long.parseLong(value.toString());
    }

    private Double toDouble(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {
            return number.doubleValue();
        }

        return Double.parseDouble(value.toString());
    }

    private String toString(Object value) {
        return value == null ? null : value.toString();
    }
}