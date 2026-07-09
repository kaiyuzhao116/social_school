package com.campusconnect.agent.controller;

import com.campusconnect.agent.dto.CampusAgentRequest;
import com.campusconnect.agent.dto.CampusCrawlRequest;
import com.campusconnect.agent.dto.CampusKnowledgeImportRequest;
import com.campusconnect.agent.service.*;
import com.campusconnect.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.campusconnect.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
@RestController
@RequestMapping("/agent/campus")
@RequiredArgsConstructor
public class CampusAgentController {

    private final EmbeddingClient embeddingClient;

    private final QdrantVectorService qdrantVectorService;

    private final CampusKnowledgeImportService campusKnowledgeImportService;
    private final CampusRagService campusRagService;

    private final CampusAgentService campusAgentService;

    private final CampusCrawlerService campusCrawlerService;

    @PostMapping("/embedding-test")
    public Result<?> embeddingTest(@RequestBody Map<String, String> body) {
        String text = body.get("text");

        List<Double> vector = embeddingClient.embed(text);

        Map<String, Object> result = new HashMap<>();
        result.put("text", text);
        result.put("dimension", vector.size());
        result.put("preview", vector.subList(0, Math.min(5, vector.size())));

        return Result.success(result);
    }

    @PostMapping("/qdrant-test")
    public Result<?> qdrantTest(@RequestBody Map<String, String> body) {
        String text = body.get("text");

        List<Double> vector = embeddingClient.embed(text);

        // 你的 embedding 维度现在是 1024
        qdrantVectorService.ensureCollection(vector.size());

        Long pointId = System.currentTimeMillis();

        Map<String, Object> payload = new HashMap<>();
        payload.put("chunkId", pointId);
        payload.put("knowledgeId", 1L);
        payload.put("title", "缓考申请流程测试");
        payload.put("sourceName", "渤海大学教务处");
        payload.put("sourceType", "教务处");
        payload.put("trustLevel", "高");
        payload.put("content", text);

        qdrantVectorService.upsertPoint(pointId, vector, payload);

        List<Map<String, Object>> results = qdrantVectorService.search(vector, 3);

        Map<String, Object> result = new HashMap<>();
        result.put("text", text);
        result.put("dimension", vector.size());
        result.put("pointId", pointId);
        result.put("searchResults", results);

        return Result.success(result);
    }
    @PostMapping("/knowledge/import")
    public Result<?> importKnowledge(@RequestBody CampusKnowledgeImportRequest request) {
        return Result.success(campusKnowledgeImportService.importKnowledge(request));
    }
    @PostMapping("/rag-test")
    public Result<?> ragTest(@RequestBody Map<String, String> body) {
        String question = body.get("question");
        return Result.success(campusRagService.retrieve(question, 5));
    }
    @PostMapping("/chat")
    public Result<?> chat(
            @RequestBody CampusAgentRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long userId = principal == null ? 0L : principal.getId();
        return Result.success(campusAgentService.chat(request, userId));
    }
    @DeleteMapping("/knowledge/reset-vector")
    public Result<?> resetVectorCollection() {
        qdrantVectorService.deleteCollection();
        return Result.success("Qdrant 校园知识库向量集合已清空");
    }
    @PostMapping("/crawler/import")
    public Result<?> crawlAndImport(@RequestBody CampusCrawlRequest request) {
        return Result.success(campusCrawlerService.crawlAndImport(request));
    }
}