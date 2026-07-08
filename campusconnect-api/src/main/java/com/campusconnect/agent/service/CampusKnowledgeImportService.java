package com.campusconnect.agent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campusconnect.agent.dto.CampusKnowledgeImportRequest;
import com.campusconnect.agent.entity.CampusKnowledge;
import com.campusconnect.agent.entity.CampusKnowledgeChunk;
import com.campusconnect.agent.mapper.CampusKnowledgeChunkMapper;
import com.campusconnect.agent.mapper.CampusKnowledgeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CampusKnowledgeImportService {

    private final CampusKnowledgeMapper campusKnowledgeMapper;
    private final CampusKnowledgeChunkMapper campusKnowledgeChunkMapper;
    private final CampusChunkService campusChunkService;
    private final EmbeddingClient embeddingClient;
    private final QdrantVectorService qdrantVectorService;

    @Transactional
    public Map<String, Object> importKnowledge(CampusKnowledgeImportRequest request) {
        if (isBlank(request.getTitle())) {
            throw new RuntimeException("标题不能为空");
        }

        if (isBlank(request.getUrl())) {
            throw new RuntimeException("来源 URL 不能为空");
        }

        if (isBlank(request.getContent())) {
            throw new RuntimeException("正文内容不能为空");
        }

        String contentHash = DigestUtils.md5DigestAsHex(
                request.getContent().getBytes(StandardCharsets.UTF_8)
        );

        CampusKnowledge existed = campusKnowledgeMapper.selectOne(
                new LambdaQueryWrapper<CampusKnowledge>()
                        .eq(CampusKnowledge::getUrl, request.getUrl())
                        .last("LIMIT 1")
        );

        if (existed != null && contentHash.equals(existed.getContentHash())) {
            Map<String, Object> result = new HashMap<>();
            result.put("knowledgeId", existed.getId());
            result.put("message", "内容未变化，无需重复导入");
            result.put("imported", false);
            return result;
        }

        CampusKnowledge knowledge;

        if (existed == null) {
            knowledge = new CampusKnowledge();
            knowledge.setCreatedAt(LocalDateTime.now());
        } else {
            knowledge = existed;
        }

        knowledge.setTitle(request.getTitle());
        knowledge.setSourceName(defaultValue(request.getSourceName(), "未知来源"));
        knowledge.setSourceType(defaultValue(request.getSourceType(), "校园通知"));
        knowledge.setUrl(request.getUrl());
        knowledge.setContent(request.getContent());
        knowledge.setContentHash(contentHash);
        knowledge.setTrustLevel(defaultValue(request.getTrustLevel(), "高"));
        knowledge.setLastCrawledAt(LocalDateTime.now());
        knowledge.setStatus("ACTIVE");
        knowledge.setUpdatedAt(LocalDateTime.now());

        if (knowledge.getId() == null) {
            campusKnowledgeMapper.insert(knowledge);
        } else {
            campusKnowledgeMapper.updateById(knowledge);

            // 简单处理：内容变了就删除旧 chunk
            campusKnowledgeChunkMapper.delete(
                    new LambdaQueryWrapper<CampusKnowledgeChunk>()
                            .eq(CampusKnowledgeChunk::getKnowledgeId, knowledge.getId())
            );

            // 注意：Qdrant 旧向量删除后面再补，现在先跑通主流程
        }

        List<String> chunks = campusChunkService.split(request.getContent());

        int successCount = 0;

        for (int i = 0; i < chunks.size(); i++) {
            String chunkText = chunks.get(i);

            CampusKnowledgeChunk chunk = new CampusKnowledgeChunk();
            chunk.setKnowledgeId(knowledge.getId());
            chunk.setChunkIndex(i);
            chunk.setContent(chunkText);
            chunk.setTokenCount(chunkText.length());
            chunk.setCreatedAt(LocalDateTime.now());

            campusKnowledgeChunkMapper.insert(chunk);

            List<Double> vector = embeddingClient.embed(chunkText);

            qdrantVectorService.ensureCollection(vector.size());

            Map<String, Object> payload = new HashMap<>();
            payload.put("chunkId", chunk.getId());
            payload.put("knowledgeId", knowledge.getId());
            payload.put("title", knowledge.getTitle());
            payload.put("sourceName", knowledge.getSourceName());
            payload.put("sourceType", knowledge.getSourceType());
            payload.put("url", knowledge.getUrl());
            payload.put("trustLevel", knowledge.getTrustLevel());
            payload.put("content", chunkText);

            qdrantVectorService.upsertPoint(chunk.getId(), vector, payload);

            chunk.setQdrantPointId(String.valueOf(chunk.getId()));
            campusKnowledgeChunkMapper.updateById(chunk);

            successCount++;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("knowledgeId", knowledge.getId());
        result.put("title", knowledge.getTitle());
        result.put("chunkCount", chunks.size());
        result.put("successCount", successCount);
        result.put("imported", true);

        return result;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String defaultValue(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : value;
    }
}