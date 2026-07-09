package com.campusconnect.agent.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CampusAgentResponse {

    private String answer;

    private String intent;

    private Double confidence;

    private List<CampusSourceDTO> sources = new ArrayList<>();

    private List<CampusTodoDTO> todos = new ArrayList<>();

    private List<CampusDeadlineDTO> deadlines = new ArrayList<>();

    /**
     * 自我反思是否通过
     */
    private Boolean reflectionPassed;

    /**
     * 风险等级：LOW / MEDIUM / HIGH
     */
    private String riskLevel;

    /**
     * 反思建议
     */
    private String reflectionSuggestion;

    /**
     * 专家协作轨迹
     */
    private List<String> expertTrace = new ArrayList<>();
}