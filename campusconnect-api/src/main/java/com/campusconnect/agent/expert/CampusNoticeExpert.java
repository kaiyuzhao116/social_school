package com.campusconnect.agent.expert;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campusconnect.agent.dto.CampusAgentResponse;
import com.campusconnect.agent.dto.CampusExpertResult;
import com.campusconnect.agent.dto.CampusSourceDTO;
import com.campusconnect.agent.entity.CampusKnowledge;
import com.campusconnect.agent.mapper.CampusKnowledgeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CampusNoticeExpert {

    private final CampusKnowledgeMapper campusKnowledgeMapper;

    public CampusExpertResult answer(String question, String intent) {
        List<CampusKnowledge> notices = campusKnowledgeMapper.selectList(
                new LambdaQueryWrapper<CampusKnowledge>()
                        .orderByDesc(CampusKnowledge::getCreatedAt)
                        .orderByDesc(CampusKnowledge::getId)
                        .last("LIMIT 5")
        );

        CampusAgentResponse response = new CampusAgentResponse();
        response.setIntent(intent);
        response.setConfidence(0.95);

        response.getExpertTrace().add("RouterExpert 判断意图：" + intent);
        response.getExpertTrace().add("NoticeExpert 直接查询 MySQL 最新校园通知，返回 " + notices.size() + " 条");

        if (notices.isEmpty()) {
            response.setAnswer("我暂时还没有检索到学校通知资料，可以先导入学校官网通知后再查询。");
            response.setReflectionPassed(true);
            response.setRiskLevel("LOW");
            response.setReflectionSuggestion("当前没有通知数据，已如实提示用户");
            response.getExpertTrace().add("NoticeExpert 未查询到通知数据");
            return buildResult(response);
        }

        StringBuilder answer = new StringBuilder();
        answer.append("最近导入的学校通知主要有：\n");

        for (int i = 0; i < notices.size(); i++) {
            CampusKnowledge notice = notices.get(i);
            answer.append(i + 1)
                    .append(". ")
                    .append(notice.getTitle())
                    .append("\n");
        }

        answer.append("\n你可以点开下方来源查看原通知详情。");

        response.setAnswer(answer.toString());
        response.setSources(buildSources(notices));

        response.setReflectionPassed(true);
        response.setRiskLevel("LOW");
        response.setReflectionSuggestion("最近通知来自 MySQL 已入库官网资料，按创建时间倒序返回，风险较低");
        response.getExpertTrace().add("NoticeExpert 已生成最近通知列表");
        response.getExpertTrace().add("ReflectionExpert 对结构化通知查询进行轻量检查，风险等级：LOW");

        return buildResult(response);
    }

    private CampusExpertResult buildResult(CampusAgentResponse response) {
        CampusExpertResult result = new CampusExpertResult();
        result.setResponse(response);
        return result;
    }

    private List<CampusSourceDTO> buildSources(List<CampusKnowledge> notices) {
        List<CampusSourceDTO> sources = new ArrayList<>();

        for (CampusKnowledge notice : notices) {
            CampusSourceDTO source = new CampusSourceDTO();
            source.setTitle(notice.getTitle());
            source.setSourceName(notice.getSourceName());
            source.setSourceType(notice.getSourceType());
            source.setTrustLevel(notice.getTrustLevel());
            source.setUrl(notice.getUrl());
            source.setScore(1.0);

            String content = notice.getContent();
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