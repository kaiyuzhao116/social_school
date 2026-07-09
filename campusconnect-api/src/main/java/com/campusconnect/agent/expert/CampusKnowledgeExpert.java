package com.campusconnect.agent.expert;

import com.campusconnect.agent.dto.CampusAgentResponse;
import com.campusconnect.agent.dto.CampusExpertResult;
import com.campusconnect.agent.dto.CampusRagChunkDTO;
import com.campusconnect.agent.dto.CampusSourceDTO;
import com.campusconnect.agent.service.CampusLlmClient;
import com.campusconnect.agent.service.CampusRagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CampusKnowledgeExpert {

    private final CampusRagService campusRagService;
    private final CampusLlmClient campusLlmClient;

    private static final double MIN_SCORE = 0.55;

    public CampusExpertResult answer(String question, String intent) {
        // 1. 先从 Qdrant 检索候选资料
        List<CampusRagChunkDTO> chunks = campusRagService.retrieve(question, 5);

        // 2. 分数过滤 + 关键词过滤，避免展示无关通知
        List<CampusRagChunkDTO> filteredChunks = chunks.stream()
                .filter(chunk -> chunk.getScore() != null && chunk.getScore() >= MIN_SCORE)
                .filter(chunk -> isRelevantToQuestion(question, chunk))
                .toList();

        // 3. 如果没有真正相关资料，直接返回资料不足，不调用大模型
        if (filteredChunks.isEmpty()) {
            CampusAgentResponse response = new CampusAgentResponse();
            response.setAnswer("当前知识库中没有检索到足够相关的学校资料，建议查看原通知或咨询学院/教务处。");
            response.setIntent(intent);
            response.setConfidence(0.1);
            response.setSources(new ArrayList<>());

            response.getExpertTrace().add("RouterExpert 判断意图：" + intent);
            response.getExpertTrace().add("KnowledgeExpert 完成知识库检索，原始召回 " + chunks.size() + " 条资料");
            response.getExpertTrace().add("KnowledgeExpert 分数过滤阈值：" + MIN_SCORE);
            response.getExpertTrace().add("KnowledgeExpert 经过分数和关键词过滤后剩余 0 条资料");
            response.getExpertTrace().add("KnowledgeExpert 返回资料不足提示，避免模型幻觉");

            CampusExpertResult result = new CampusExpertResult();
            result.setResponse(response);
            result.setChunks(filteredChunks);

            return result;
        }

        // 4. 只把真正相关的资料交给大模型
        CampusAgentResponse response = campusLlmClient.generateAnswer(question, filteredChunks);

        response.setIntent(intent);
        response.setSources(buildSources(filteredChunks));

        if (response.getConfidence() == null) {
            response.setConfidence(0.8);
        }

        response.getExpertTrace().add("RouterExpert 判断意图：" + intent);
        response.getExpertTrace().add("KnowledgeExpert 完成知识库检索，原始召回 " + chunks.size() + " 条资料");
        response.getExpertTrace().add("KnowledgeExpert 分数过滤阈值：" + MIN_SCORE);
        response.getExpertTrace().add("KnowledgeExpert 经过分数和关键词过滤后剩余 " + filteredChunks.size() + " 条资料");
        response.getExpertTrace().add("KnowledgeExpert 基于过滤后的资料生成初稿回答");

        CampusExpertResult result = new CampusExpertResult();
        result.setResponse(response);
        result.setChunks(filteredChunks);

        return result;
    }

    /**
     * 简单关键词相关性判断：
     * 防止问“缓考”，却展示党建、挑战杯、合作项目这种无关通知。
     */
    private boolean isRelevantToQuestion(String question, CampusRagChunkDTO chunk) {
        if (question == null || chunk == null) {
            return false;
        }

        String text = safe(chunk.getTitle())
                + " "
                + safe(chunk.getContent())
                + " "
                + safe(chunk.getSourceName())
                + " "
                + safe(chunk.getSourceType());

        // 缓考类问题
        if (question.contains("缓考")) {
            return text.contains("缓考")
                    || text.contains("考试")
                    || text.contains("教务")
                    || text.contains("申请表");
        }

        // 成绩证明类问题
        if (question.contains("成绩证明") || question.contains("成绩")) {
            return text.contains("成绩")
                    || text.contains("成绩证明")
                    || text.contains("教务")
                    || text.contains("证明");
        }

        // 截止时间类问题
        if (question.contains("截止") || question.contains("什么时候") || question.contains("时间")) {
            return text.contains("截止")
                    || text.contains("时间")
                    || text.contains("日期")
                    || text.contains("前")
                    || text.contains("提交");
        }

        // 总结通知类问题
        if (question.contains("总结") || question.contains("三句话") || question.contains("通知")) {
            return text.contains("通知")
                    || text.contains("公告")
                    || text.contains("安排")
                    || text.contains("公示");
        }

        // 和我有关类问题
        if (question.contains("和我有关") || question.contains("我需要") || question.contains("是否相关")) {
            return text.contains("学生")
                    || text.contains("本科生")
                    || text.contains("研究生")
                    || text.contains("学院")
                    || text.contains("报名")
                    || text.contains("提交");
        }

        // 默认情况下，只要分数过线就保留
        return true;
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

    private String safe(String value) {
        return value == null ? "" : value;
    }
}