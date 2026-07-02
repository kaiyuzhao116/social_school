package com.campusconnect.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageVO {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private String content;
    private String type;
    private LocalDateTime createdAt;
    private Boolean isSelf; // 是否是自己发送的消息
}
