package com.campusconnect.chat.service.impl;

import com.campusconnect.chat.dto.ChatSendMessageDTO;
import com.campusconnect.chat.entity.ChatMessage;
import com.campusconnect.chat.mapper.ChatMapper;
import com.campusconnect.chat.service.ChatService;
import com.campusconnect.chat.vo.ChatMessageVO;
import com.campusconnect.chat.vo.ChatRoomVO;
import com.campusconnect.chat.websocket.ChatWebSocketSessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.campusconnect.chat.mq.ChatMessageEvent;
import com.campusconnect.chat.mq.ChatMessageProducer;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final ChatMessageProducer chatMessageProducer;
    private final ChatMapper chatMapper;
    private final ChatWebSocketSessionManager chatWebSocketSessionManager;
    private final ObjectMapper objectMapper;
    @Override
    public List<ChatRoomVO> getChatRooms(Long userId) {
        return chatMapper.selectChatRoomsByUserId(userId);
    }

    @Override
    public List<ChatMessageVO> getChatMessages(Long conversationId) {
        return chatMapper.selectMessagesByConversationId(conversationId);
    }
    @Override
    @Transactional
    public void markConversationRead(Long userId, Long conversationId) {
        Integer memberCount = chatMapper.countConversationMember(conversationId, userId);

        if (memberCount == null || memberCount <= 0) {
            throw new RuntimeException("你不在该聊天室中，不能标记已读");
        }

        Long latestMessageId = chatMapper.selectLatestMessageId(conversationId);

        chatMapper.markConversationRead(conversationId, userId, latestMessageId);
    }
    @Override
    @Transactional
    public ChatMessageVO sendMessage(Long userId, ChatSendMessageDTO dto) {
        if (dto.getConversationId() == null) {
            throw new RuntimeException("会话ID不能为空");
        }

        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            throw new RuntimeException("消息内容不能为空");
        }

        Integer memberCount = chatMapper.countConversationMember(dto.getConversationId(), userId);
        if (memberCount == null || memberCount <= 0) {
            throw new RuntimeException("你不在该聊天室中，不能发送消息");
        }

        ChatMessage message = new ChatMessage();
        message.setConversationId(dto.getConversationId());
        message.setSenderId(userId);
        message.setClientMsgId(
                dto.getClientMsgId() == null || dto.getClientMsgId().isBlank()
                        ? UUID.randomUUID().toString()
                        : dto.getClientMsgId()
        );
        message.setMessageType(dto.getMessageType() == null ? 1 : dto.getMessageType());
        message.setContent(dto.getContent().trim());
        message.setStatus(1);
        message.setSendTime(LocalDateTime.now());

        chatMapper.insertChatMessage(message);

        chatMapper.updateConversationLastMessage(
                dto.getConversationId(),
                message.getId(),
                message.getContent()
        );

        ChatMessageVO messageVO = chatMapper.selectMessageById(message.getId());

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                chatMessageProducer.sendMessageCreated(
                        new ChatMessageEvent(
                                dto.getConversationId(),
                                userId,
                                message.getId()
                        )
                );
            }
        });

        return messageVO;
    }
    /**
     * 推送消息给聊天室其他在线成员
     */
    private void pushMessageToOnlineMembers(Long conversationId, Long senderId, ChatMessageVO messageVO) {
        try {
            List<Long> receiverIds = chatMapper.selectOtherMemberIds(conversationId, senderId);

            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "CHAT_MESSAGE");
            payload.put("data", messageVO);

            String json = objectMapper.writeValueAsString(payload);

            for (Long receiverId : receiverIds) {
                chatWebSocketSessionManager.sendToUser(receiverId, json);
            }
        } catch (Exception e) {
            // WebSocket 推送失败不能影响消息落库
            System.err.println("WebSocket 推送失败：" + e.getMessage());
        }
    }
}