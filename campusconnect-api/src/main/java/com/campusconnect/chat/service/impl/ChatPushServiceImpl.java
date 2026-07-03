package com.campusconnect.chat.service.impl;

import com.campusconnect.chat.mapper.ChatMapper;
import com.campusconnect.chat.service.ChatPushService;
import com.campusconnect.chat.vo.ChatMessageVO;
import com.campusconnect.chat.websocket.ChatWebSocketSessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatPushServiceImpl implements ChatPushService {

    private final ChatMapper chatMapper;

    private final ChatWebSocketSessionManager chatWebSocketSessionManager;

    private final ObjectMapper objectMapper;

    @Override
    public void pushMessage(Long conversationId, Long senderId, Long messageId) {
        try {
            ChatMessageVO messageVO = chatMapper.selectMessageById(messageId);

            if (messageVO == null) {
                log.warn("消息不存在，取消推送，messageId={}", messageId);
                return;
            }

            List<Long> receiverIds = chatMapper.selectOtherMemberIds(conversationId, senderId);

            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "CHAT_MESSAGE");
            payload.put("data", messageVO);

            String json = objectMapper.writeValueAsString(payload);

            for (Long receiverId : receiverIds) {
                chatWebSocketSessionManager.sendToUser(receiverId, json);
            }

            log.info("聊天消息推送完成，conversationId={}, senderId={}, messageId={}",
                    conversationId, senderId, messageId);

        } catch (Exception e) {
            log.error("聊天消息推送失败，conversationId={}, senderId={}, messageId={}",
                    conversationId, senderId, messageId, e);
        }
    }
}