package com.campusconnect.agent.expert;

import com.campusconnect.agent.dto.*;
import com.campusconnect.agent.service.CampusLlmClient;
import com.campusconnect.agent.service.CampusRagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CampusKnowledgeExpert {

    private final CampusRagService campusRagService;
    private final CampusLlmClient campusLlmClient;

    public CampusExpertResult answer(String question, String intent) {
        List<CampusRagChunkDTO> chunks = campusRagService.retrieve(question, 5);

        CampusAgentResponse response = campusLlmClient.generateAnswer(question, chunks);

        response.setIntent(intent);
        response.setSources(buildSources(chunks));

        if (response.getConfidence() == null) {
            response.setConfidence(0.8);
        }

        response.getExpertTrace().add("RouterExpert 判断意图：" + intent);
        response.getExpertTrace().add("KnowledgeExpert 完成知识库检索，召回 " + chunks.size() + " 条资料");
        response.getExpertTrace().add("KnowledgeExpert 基于检索资料生成初稿回答");

        CampusExpertResult result = new CampusExpertResult();
        result.setResponse(response);
        result.setChunks(chunks);

        return result;
    }

    private List<CampusSourceDTO> buildSources(List<CampusRagChunkDTO> chunks) {
        List<CampusSourceDTO> sources = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (CampusRagChunkDTO chunk : chunks) {
            String key = chunk.getKnowledgeId() + "-" + chunk.getTitle();

            if (seen.contains(key)) {
                continue;
            }

            seen.add(key);

            CampusSourceDTO source = new CampusSourceDTO();
            source.setTitle(chunk.getTitle());
            source.setSourceName(chunk.getSourceName());
            source.setSourceType(chunk.getSourceType());
            source.setUrl(chunk.getUrl());
            source.setTrustLevel(chunk.getTrustLevel());
            source.setScore(chunk.getScore());

            String content = chunk.getContent();
            if (content != null && content.length() > 80) {
                source.setContentPreview(content.substring(0, 80) + "...");
            } else {
                source.setContentPreview(content);
            }

            sources.add(source);
        }

        return sources;
    }
}