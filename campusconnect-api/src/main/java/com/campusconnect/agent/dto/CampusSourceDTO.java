package com.campusconnect.agent.dto;

import lombok.Data;

@Data
public class CampusSourceDTO {
    private String title;
    private String sourceName;
    private String sourceType;
    private String url;
    private String trustLevel;
    private Double score;
    private String contentPreview;
}