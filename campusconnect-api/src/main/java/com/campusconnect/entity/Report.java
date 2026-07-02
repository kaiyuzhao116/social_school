package com.campusconnect.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("report")
public class Report {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long reporterId;     // 举报人ID
    private Long targetId;       // 被举报对象ID
    private String targetType;   // POST-帖子, COMMENT-评论, USER-用户
    private String reason;       // 举报原因
    private String description;  // 详细描述
    
    // 状态: PENDING-待处理, RESOLVED-已解决, IGNORED-已忽略
    private String status;
    
    private String action;       // 处理动作: DELETE-删除, BAN-封禁, WARN-警告
    private Long handlerId;      // 处理人ID
    private LocalDateTime handledAt;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    // 非数据库字段
    @TableField(exist = false)
    private User reporter;
    
    @TableField(exist = false)
    private Post targetPost;
}
