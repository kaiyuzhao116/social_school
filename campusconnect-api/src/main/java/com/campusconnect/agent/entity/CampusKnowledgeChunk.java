package com.campusconnect.agent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("campus_knowledge_chunk")
public class CampusKnowledgeChunk {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long knowledgeId;

    private Integer chunkIndex;

    private String content;

    private String qdrantPointId;

    private Integer tokenCount;

    private LocalDateTime createdAt;
}