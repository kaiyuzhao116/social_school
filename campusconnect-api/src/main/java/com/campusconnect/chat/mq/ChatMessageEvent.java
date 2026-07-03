package com.campusconnect.chat.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageEvent {

    /**
     * 聊天室ID
     */
    private Long conversationId;

    /**
     * 发送人ID
     */
    private Long senderId;

    /**
     * 消息ID
     */
    private Long messageId;
}