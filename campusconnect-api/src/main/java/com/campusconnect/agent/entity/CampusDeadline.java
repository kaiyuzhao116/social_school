package com.campusconnect.agent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("campus_deadline")
public class CampusDeadline {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long knowledgeId;

    private String title;

    private String sourceTitle;

    private String sourceUrl;

    private String sourceType;

    private LocalDateTime deadline;

    private String targetGroup;

    private String importance;

    private String extractedBy;

    private LocalDateTime createdAt;
}