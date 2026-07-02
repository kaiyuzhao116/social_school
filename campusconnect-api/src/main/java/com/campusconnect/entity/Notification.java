package com.campusconnect.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("notification")
public class Notification {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;         // 接收者ID
    private Long senderId;       // 发送者ID (系统通知为null)
    
    // 类型: LIKE-点赞, COMMENT-评论, FOLLOW-关注, SYSTEM-系统, ADMIN-管理
    private String type;
    
    private String title;
    private String content;
    
    private Long targetId;       // 关联对象ID
    private String targetType;   // POST-帖子, COMMENT-评论, USER-用户
    
    private Boolean isRead;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    // 非数据库字段
    @TableField(exist = false)
    private User sender;
}
