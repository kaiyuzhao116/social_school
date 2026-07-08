package com.campusconnect.agent.dto;

import lombok.Data;

@Data
public class CampusRagChunkDTO {

    private Long chunkId;

    private Long knowledgeId;

    private String title;

    private String sourceName;

    private String sourceType;

    private String url;

    private String trustLevel;

    private String content;

    private Double score;
}