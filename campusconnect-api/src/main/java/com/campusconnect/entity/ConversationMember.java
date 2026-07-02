package com.campusconnect.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("conversation_member")
public class ConversationMember {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long conversationId;
    private Long userId;
    private String role; // OWNER, ADMIN, MEMBER
    private Integer unreadCount; // 未读消息数
    private LocalDateTime lastReadAt; // 最后阅读时间
    private LocalDateTime joinedAt; // 加入时间
}
