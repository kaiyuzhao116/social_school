package com.campusconnect.agent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campusconnect.agent.dto.CampusKnowledgeImportRequest;
import com.campusconnect.agent.entity.CampusKnowledge;
import com.campusconnect.agent.entity.CampusKnowledgeChunk;
import com.campusconnect.agent.mapper.CampusKnowledgeChunkMapper;
import com.campusconnect.agent.mapper.CampusKnowledgeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
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

        /*
         * 用 title + content 算 hash。
         * 用于 URL 变化但正文相同的情况。
         */
        String contentHash = DigestUtils.md5DigestAsHex(
                (request.getTitle() + "\n" + request.getContent())
                        .getBytes(StandardCharsets.UTF_8)
        );

        /*
         * 1. URL 去重：
         * 只要 URL 已存在，就直接跳过。
         * 这样自动爬虫不会因为页面模板、浏览量、动态字段变化而反复更新和重建向量。
         */
        CampusKnowledge existedByUrl = campusKnowledgeMapper.selectOne(
                new LambdaQueryWrapper<CampusKnowledge>()
                        .eq(CampusKnowledge::getUrl, request.getUrl())
                        .last("LIMIT 1")
        );

        if (existedByUrl != null) {
            existedByUrl.setLastCrawledAt(LocalDateTime.now());
            existedByUrl.setUpdatedAt(LocalDateTime.now());
            campusKnowledgeMapper.updateById(existedByUrl);

            log.info("知识 URL 已存在，跳过重复导入，url={}", request.getUrl());

            Map<String, Object> result = new HashMap<>();
            result.put("knowledgeId", existedByUrl.getId());
            result.put("title", existedByUrl.getTitle());
            result.put("imported", false);
            result.put("updated", false);
            result.put("skipReason", "URL 已存在");
            result.put("message", "URL 已存在，无需重复导入");
            return result;
        }

        /*
         * 2. 内容 Hash 去重：
         * 如果 URL 不同，但 title + content 一样，也跳过。
         * 防止同一篇通知因为代理链接变化而重复入库。
         */
        CampusKnowledge existedByHash = campusKnowledgeMapper.selectOne(
                new LambdaQueryWrapper<CampusKnowledge>()
                        .eq(CampusKnowledge::getContentHash, contentHash)
                        .last("LIMIT 1")
        );

        if (existedByHash != null) {
            log.info("知识内容已存在，跳过导入，title={}, existedId={}",
                    request.getTitle(),
                    existedByHash.getId());

            Map<String, Object> result = new HashMap<>();
            result.put("knowledgeId", existedByHash.getId());
            result.put("title", existedByHash.getTitle());
            result.put("imported", false);
            result.put("updated", false);
            result.put("skipReason", "内容 Hash 已存在");
            result.put("message", "相同内容已存在，无需重复导入");
            return result;
        }

        /*
         * 3. 新增知识
         */
        CampusKnowledge knowledge = new CampusKnowledge();
        knowledge.setTitle(request.getTitle());
        knowledge.setSourceName(defaultValue(request.getSourceName(), "未知来源"));
        knowledge.setSourceType(defaultValue(request.getSourceType(), "校园通知"));
        knowledge.setUrl(request.getUrl());
        knowledge.setContent(request.getContent());
        knowledge.setContentHash(contentHash);
        knowledge.setTrustLevel(defaultValue(request.getTrustLevel(), "高"));
        knowledge.setLastCrawledAt(LocalDateTime.now());
        knowledge.setStatus("ACTIVE");
        knowledge.setCreatedAt(LocalDateTime.now());
        knowledge.setUpdatedAt(LocalDateTime.now());

        campusKnowledgeMapper.insert(knowledge);

        log.info("新增校园知识：id={}, title={}", knowledge.getId(), knowledge.getTitle());

        /*
         * 4. 文本切片
         */
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

            /*
             * 5. Embedding 向量化
             */
            List<Double> vector = embeddingClient.embed(chunkText);

            /*
             * 6. 确保 Qdrant collection 存在
             */
            qdrantVectorService.ensureCollection(vector.size());

            /*
             * 7. 写入 Qdrant
             */
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
        result.put("updated", false);
        result.put("message", "知识导入成功");

        return result;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String defaultValue(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : value;
    }
}