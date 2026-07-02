package com.campusconnect.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("post")
public class Post {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    private String content;
    private String images;  // JSON数组格式存储多张图片URL
    private String tags;    // JSON数组格式存储标签
    
    // 状态: PUBLISHED-已发布, PENDING-审核中, REJECTED-已拒绝, FLAGGED-被标记
    private String status;
    
    private Boolean isPinned;
    private Boolean isAnonymous;
    
    private Integer likeCount;
    private Integer commentCount;
    private Integer shareCount;
    private Integer viewCount;
    
    // AI审核结果
    private Boolean aiSafe;
    private String aiReason;
    private Integer aiConfidence;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    @TableLogic
    private Integer deleted;
    
    // 非数据库字段
    @TableField(exist = false)
    private User author;
    
    @TableField(exist = false)
    private Boolean isLiked;
    
    @TableField(exist = false)
    private Boolean isCollected;
}
