package com.campusconnect.agent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("campus_knowledge")
public class CampusKnowledge {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String sourceName;

    private String sourceType;

    private String url;

    private String content;

    private String contentHash;

    private String trustLevel;

    private LocalDateTime publishTime;

    private LocalDateTime lastCrawledAt;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}