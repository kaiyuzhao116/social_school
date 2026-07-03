package com.campusconnect.chat.dto;

import lombok.Data;

@Data
public class ChatSendMessageDTO {

    /**
     * 会话ID
     */
    private Long conversationId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型：1文本，2图片，3文件，4系统消息
     */
    private Integer messageType;

    /**
     * 前端生成的消息ID，用于幂等
     */
    private String clientMsgId;
}