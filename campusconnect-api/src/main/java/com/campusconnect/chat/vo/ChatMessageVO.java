package com.campusconnect.chat.vo;

import lombok.Data;

@Data
public class ChatMessageVO {

    /**
     * 消息ID
     */
    private Long messageId;

    /**
     * 会话ID
     */
    private Long conversationId;

    /**
     * 发送人ID
     */
    private Long senderId;

    /**
     * 发送人昵称
     */
    private String username;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型：1文本，2图片，3文件，4系统消息
     */
    private Integer messageType;

    /**
     * 消息日期，例如 2026-07-03
     */
    private String messageDate;

    /**
     * 消息时间，例如 16:42
     */
    private String messageTime;
}