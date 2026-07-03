package com.campusconnect.chat.service;

public interface ChatPushService {

    void pushMessage(Long conversationId, Long senderId, Long messageId);
}