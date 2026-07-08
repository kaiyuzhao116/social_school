package com.campusconnect.agent.service;

import com.campusconnect.agent.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CampusAgentService {

    private final CampusRagService campusRagService;
    private final CampusLlmClient campusLlmClient;

    public CampusAgentResponse chat(CampusAgentRequest request) {
        if (request == null || isBlank(request.getQuestion())) {
            throw new RuntimeException("问题不能为空");
        }

        String question = request.getQuestion();

        List<CampusRagChunkDTO> chunks = campusRagService.retrieve(question, 5);

        CampusAgentResponse response = campusLlmClient.generateAnswer(question, chunks);

        response.setSources(buildSources(chunks));

        if (response.getConfidence() == null) {
            response.setConfidence(0.8);
        }

        return response;
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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}