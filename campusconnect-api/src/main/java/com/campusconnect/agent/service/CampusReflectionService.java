package com.campusconnect.agent.service;

import com.campusconnect.agent.dto.CampusAgentResponse;
import com.campusconnect.agent.dto.CampusRagChunkDTO;
import com.campusconnect.agent.dto.CampusReflectionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CampusReflectionService {

    private final CampusLlmClient campusLlmClient;

    public CampusAgentResponse reflect(
            String question,
            List<CampusRagChunkDTO> chunks,
            CampusAgentResponse draftResponse
    ) {
        try {
            CampusReflectionResult reflectionResult =
                    campusLlmClient.reflectAnswer(question, chunks, draftResponse);

            Boolean passed = reflectionResult.getPassed();

            draftResponse.setReflectionPassed(passed);
            draftResponse.setRiskLevel(defaultValue(reflectionResult.getRiskLevel(), "LOW"));
            draftResponse.setReflectionSuggestion(defaultValue(
                    reflectionResult.getSuggestion(),
                    "已完成反思检查"
            ));

            draftResponse.getExpertTrace().add(
                    "ReflectionExpert 完成自我反思检查，风险等级："
                            + draftResponse.getRiskLevel()
            );

            if (Boolean.FALSE.equals(passed)
                    && !isBlank(reflectionResult.getRevisedAnswer())) {
                draftResponse.setAnswer(reflectionResult.getRevisedAnswer());
                draftResponse.getExpertTrace().add("ReflectionExpert 发现初稿风险，已替换为更稳妥回答");
            }

            return draftResponse;
        } catch (Exception e) {
            draftResponse.setReflectionPassed(false);
            draftResponse.setRiskLevel("UNKNOWN");
            draftResponse.setReflectionSuggestion("自我反思检查失败，已保留初稿回答：" + e.getMessage());
            draftResponse.getExpertTrace().add("ReflectionExpert 检查失败，保留初稿回答");

            return draftResponse;
        }
    }

    private String defaultValue(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}