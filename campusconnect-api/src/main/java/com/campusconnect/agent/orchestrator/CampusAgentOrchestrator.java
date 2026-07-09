package com.campusconnect.agent.orchestrator;

import com.campusconnect.agent.dto.CampusAgentRequest;
import com.campusconnect.agent.dto.CampusAgentResponse;
import com.campusconnect.agent.dto.CampusExpertResult;
import com.campusconnect.agent.expert.CampusKnowledgeExpert;
import com.campusconnect.agent.expert.CampusNoticeExpert;
import com.campusconnect.agent.expert.CampusRouterExpert;
import com.campusconnect.agent.service.CampusAgentMemoryService;
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
    private final CampusAgentMemoryService campusAgentMemoryService;

    public CampusAgentResponse chat(CampusAgentRequest request, Long userId) {
        if (request == null || isBlank(request.getQuestion())) {
            throw new RuntimeException("问题不能为空");
        }

        Long safeUserId = userId == null ? 0L : userId;
        String question = request.getQuestion();

        // 1. 读取记忆。注意：这里先读取旧记忆，再保存当前问题，避免当前问题重复进入上下文。
        String memoryContext = campusAgentMemoryService.buildMemoryContext(safeUserId);

        // 2. Router 专家判断意图
        String intent = campusRouterExpert.route(question);

        CampusExpertResult expertResult;

        // 3. 根据意图分派专家
        if ("NOTICE_QUERY".equals(intent)) {
            expertResult = campusNoticeExpert.answer(question, intent);
        } else {
            expertResult = campusKnowledgeExpert.answer(question, intent, memoryContext);
        }

        CampusAgentResponse draftResponse = expertResult.getResponse();

        CampusAgentResponse finalResponse;

        // 4. NOTICE_QUERY 是结构化 MySQL 查询，不走大模型反思，保证速度
        if ("NOTICE_QUERY".equals(intent)) {
            finalResponse = draftResponse;
        } else {
            finalResponse = campusReflectionService.reflect(
                    question,
                    expertResult.getChunks(),
                    draftResponse
            );
        }

        // 5. 保存当前问题到短期记忆，只保留最近 10 次提问
        campusAgentMemoryService.saveRecentQuestion(safeUserId, question);

        // 6. 如果用户表达了长期信息，则写入长期记忆
        campusAgentMemoryService.maybeSaveLongTermMemory(safeUserId, question);

        if (!isBlank(memoryContext)) {
            finalResponse.getExpertTrace().add("MemoryService 已注入最近提问和长期记忆，上限 10 条最近提问");
        }

        finalResponse.getExpertTrace().add("Orchestrator 完成多专家协作并返回最终结果");

        return finalResponse;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}