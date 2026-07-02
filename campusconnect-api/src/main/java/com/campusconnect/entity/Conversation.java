package com.campusconnect.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("conversation")
public class Conversation {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name; // 群聊名称，私聊为空
    private String type; // PRIVATE, GROUP
    private String avatar; // 群聊头像
    private Long ownerId; // 群主ID
    private String announcement; // 群公告

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
