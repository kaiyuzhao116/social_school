package com.campusconnect.agent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("campus_agent_memory")
public class CampusAgentMemory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String memoryKey;

    private String memoryValue;

    /**
     * PROFILE / PREFERENCE / NOTE
     */
    private String memoryType;

    /**
     * USER / AGENT
     */
    private String source;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}