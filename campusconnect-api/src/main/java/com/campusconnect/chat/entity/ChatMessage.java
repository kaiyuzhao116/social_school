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
}