package com.campusconnect.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_follow")
public class UserFollow {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long followerId; // 关注者ID
    private Long followingId; // 被关注者ID

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
