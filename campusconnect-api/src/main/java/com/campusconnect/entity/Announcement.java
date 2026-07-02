package com.campusconnect.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("announcement")
public class Announcement {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String title;
    private String content;
    private String publisher;    // 发布部门
    private Long publisherId;    // 发布人ID
    
    // 状态: DRAFT-草稿, PUBLISHED-已发布
    private String status;
    
    // 优先级: NORMAL-普通, PINNED-置顶
    private String priority;
    
    private Integer viewCount;
    
    private LocalDateTime publishedAt;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    @TableLogic
    private Integer deleted;
}
