package com.campusconnect.agent.orchestrator;

import com.campusconnect.agent.dto.CampusAgentRequest;
import com.campusconnect.agent.dto.CampusAgentResponse;
import com.campusconnect.agent.dto.CampusExpertResult;
import com.campusconnect.agent.expert.CampusKnowledgeExpert;
import com.campusconnect.agent.expert.CampusRouterExpert;
import com.campusconnect.agent.service.CampusReflectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CampusAgentOrchestrator {

    private final CampusRouterExpert campusRouterExpert;
    private final CampusKnowledgeExpert campusKnowledgeExpert;
    private final CampusReflectionService campusReflectionService;

    public CampusAgentResponse chat(CampusAgentRequest request) {
        if (request == null || isBlank(request.getQuestion())) {
            throw new RuntimeException("问题不能为空");
        }

        String question = request.getQuestion();

        // 1. Router 专家判断意图
        String intent = campusRouterExpert.route(question);

        // 2. Knowledge 专家检索知识库并生成初稿
        CampusExpertResult expertResult = campusKnowledgeExpert.answer(question, intent);

        CampusAgentResponse draftResponse = expertResult.getResponse();

        // 3. Reflection 专家做自我反思
        CampusAgentResponse finalResponse = campusReflectionService.reflect(
                question,
                expertResult.getChunks(),
                draftResponse
        );

        finalResponse.getExpertTrace().add("Orchestrator 完成多专家协作并返回最终结果");

        return finalResponse;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}