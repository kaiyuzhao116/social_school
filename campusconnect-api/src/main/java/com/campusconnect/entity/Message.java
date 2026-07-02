package com.campusconnect.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("message")
public class Message {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long conversationId;
    private Long senderId;
    private String content;
    private String type; // TEXT, IMAGE, FILE

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
