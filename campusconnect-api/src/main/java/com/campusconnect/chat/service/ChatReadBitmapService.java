package com.campusconnect.chat.service;

public interface ChatReadBitmapService {

    /**
     * 标记某个用户已读某条消息
     */
    void markMessageRead(Long messageId, Long userId);

    /**
     * 判断某个用户是否读过某条消息
     */
    Boolean isMessageRead(Long messageId, Long userId);

    /**
     * 统计某条消息已读人数
     */
    Long countMessageRead(Long messageId);
}