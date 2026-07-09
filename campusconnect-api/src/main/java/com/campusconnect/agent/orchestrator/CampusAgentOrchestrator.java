package com.campusconnect.agent.orchestrator;

import com.campusconnect.agent.dto.CampusAgentRequest;
import com.campusconnect.agent.dto.CampusAgentResponse;
import com.campusconnect.agent.dto.CampusExpertResult;
import com.campusconnect.agent.expert.CampusKnowledgeExpert;
import com.campusconnect.agent.expert.CampusNoticeExpert;
import com.campusconnect.agent.expert.CampusRouterExpert;
import com.campusconnect.agent.service.CampusReflectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CampusAgentOrchestrator {

    private final CampusRouterExpert campusRouterExpert;
    private final CampusKnowledgeExpert campusKnowledgeExpert;
    private final CampusNoticeExpert campusNoticeExpert;
    private final CampusReflectionService campusReflectionService;

    public CampusAgentResponse chat(CampusAgentRequest request) {
        if (request == null || isBlank(request.getQuestion())) {
            throw new RuntimeException("问题不能为空");
        }

        String question = request.getQuestion();

        // 1. Router 专家判断意图
        String intent = campusRouterExpert.route(question);

        CampusExpertResult expertResult;

        // 2. 根据意图分派给不同专家
        if ("NOTICE_QUERY".equals(intent)) {
            expertResult = campusNoticeExpert.answer(question, intent);
        } else {
            expertResult = campusKnowledgeExpert.answer(question, intent);
        }

        CampusAgentResponse draftResponse = expertResult.getResponse();

        // 3. NOTICE_QUERY 已经是结构化查询，不需要再调用大模型反思，避免慢
        if ("NOTICE_QUERY".equals(intent)) {
            draftResponse.getExpertTrace().add("Orchestrator 完成多专家协作并返回最终结果");
            return draftResponse;
        }

        // 4. 其他复杂问答再走自我反思
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