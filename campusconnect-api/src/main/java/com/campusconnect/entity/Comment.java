package com.campusconnect.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("comment")
public class Comment {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long postId;
    private Long userId;
    private Long parentId;       // 父评论ID，用于回复
    private Long replyToUserId;  // 回复的用户ID
    private String content;
    private Integer likeCount;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableLogic
    private Integer deleted;
    
    // 非数据库字段
    @TableField(exist = false)
    private User author;
    
    @TableField(exist = false)
    private User replyToUser;
    
    @TableField(exist = false)
    private List<Comment> replies;
    
    @TableField(exist = false)
    private Boolean isLiked;
}
