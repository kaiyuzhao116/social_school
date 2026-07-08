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
}