package com.campusconnect.dto;

import lombok.Data;

@Data
public class SendMessageRequest {
    private Long conversationId;
    private Long targetUserId; // 私聊时使用
    private String content;
    private String type = "TEXT";
}
