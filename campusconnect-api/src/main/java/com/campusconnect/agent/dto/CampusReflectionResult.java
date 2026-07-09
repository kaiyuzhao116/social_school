package com.campusconnect.agent.dto;

import lombok.Data;

@Data
public class CampusReflectionResult {

    /**
     * 是否通过反思检查
     */
    private Boolean passed;

    /**
     * 风险等级：LOW / MEDIUM / HIGH
     */
    private String riskLevel;

    /**
     * 反思建议
     */
    private String suggestion;

    /**
     * 如果原回答有问题，可以给出修正版回答
     */
    private String revisedAnswer;
}