package com.campusconnect.chat.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessage {

    private Long id;

    private Long conversationId;

    private Long senderId;

    private String clientMsgId;

    private Integer messageType;

    private String content;

    private Integer status;

    private LocalDateTime sendTime;

    /**
     * 是否阅后即焚：0否，1是
     */
    private Integer burnAfterRead;

    /**
     * 是否已焚毁：0否，1是
     */
    private Integer burned;
    /**
     * 焚毁时间
     */
    private LocalDateTime burnedTime;

    /**
     * 触发焚毁的用户ID
     */
    private Long burnTriggerUserId;
}