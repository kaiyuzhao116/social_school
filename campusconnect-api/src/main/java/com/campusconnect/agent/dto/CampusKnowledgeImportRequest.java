package com.campusconnect.agent.dto;

import lombok.Data;

@Data
public class CampusKnowledgeImportRequest {

    private String title;

    private String sourceName;

    private String sourceType;

    private String url;

    private String content;

    private String trustLevel;
}