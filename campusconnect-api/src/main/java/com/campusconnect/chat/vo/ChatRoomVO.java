package com.campusconnect.chat.vo;

import lombok.Data;

@Data
public class ChatRoomVO {

    /**
     * 聊天室ID
     */
    private Long roomId;

    /**
     * 聊天室名称
     */
    private String roomName;

    /**
     * 聊天室头像
     */
    private String avatar;

    /**
     * 未读数量
     */
    private Integer unreadCount;

    /**
     * 最后一条消息内容
     */
    private String lastMessageContent;

    /**
     * 最后一条消息时间，格式 HH:mm
     */
    private String lastMessageTime;

    private Integer type;
}