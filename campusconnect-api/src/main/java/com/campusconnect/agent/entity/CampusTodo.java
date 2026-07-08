package com.campusconnect.agent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("campus_todo")
public class CampusTodo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    private String description;

    private String sourceTitle;

    private String sourceUrl;

    private LocalDateTime deadline;

    private String status;

    private String priority;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}