package com.campusconnect.chat.entity;

import lombok.Data;

@Data
public class ChatConversation {

    private Long id;

    /**
     * 1 单聊，2 群聊
     */
    private Integer type;

    /**
     * 私聊唯一标识，小用户ID_大用户ID
     */
    private String privateKey;

    private String name;

    private Long ownerId;

    private Integer status;
}