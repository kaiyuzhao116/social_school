package com.campusconnect.chat.service.impl;

import com.campusconnect.chat.dto.ChatSendMessageDTO;
import com.campusconnect.chat.entity.ChatConversation;
import com.campusconnect.chat.entity.ChatMessage;
import com.campusconnect.chat.mapper.ChatMapper;
import com.campusconnect.chat.service.ChatReadBitmapService;
import com.campusconnect.chat.service.ChatService;
import com.campusconnect.chat.vo.ChatMessageReadUserVO;
import com.campusconnect.chat.vo.ChatMessageVO;
import com.campusconnect.chat.vo.ChatRoomVO;
import com.campusconnect.chat.vo.PrivateChatUserVO;
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
    private final ChatReadBitmapService chatReadBitmapService;
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
        if (conversationId == null) {
            throw new RuntimeException("会话ID不能为空");
        }

        Integer memberCount = chatMapper.countConversationMember(conversationId, userId);
        if (memberCount == null || memberCount <= 0) {
            throw new RuntimeException("你不在该聊天室中，不能标记已读");
        }

        Long latestMessageId = chatMapper.selectLatestMessageId(conversationId);

        if (latestMessageId == null) {
            latestMessageId = 0L;
        }

        chatMapper.markConversationRead(
                conversationId,
                userId,
                latestMessageId
        );

        if (latestMessageId <= 0) {
            return;
        }

        List<Long> messageIds = chatMapper.selectMessageIdsBeforeLatest(
                conversationId,
                latestMessageId
        );

        for (Long messageId : messageIds) {
            // MySQL：记录谁读了哪条消息
            chatMapper.insertMessageReadRecord(
                    messageId,
                    conversationId,
                    userId
            );

            // Redis Bitmap：快速统计这条消息多少人已读
            chatReadBitmapService.markMessageRead(
                    messageId,
                    userId
            );
        }
    }

    @Override
    @Transactional
    public ChatRoomVO createOrGetPrivateConversation(Long userId, Long targetUserId) {
        if (targetUserId == null) {
            throw new RuntimeException("目标用户不能为空");
        }

        if (userId.equals(targetUserId)) {
            throw new RuntimeException("不能和自己创建私聊");
        }

        String privateKey = buildPrivateKey(userId, targetUserId);

        Long conversationId = chatMapper.selectPrivateConversationIdByKey(privateKey);

        // 已经有私聊会话，直接返回
        if (conversationId != null) {
            return chatMapper.selectChatRoomByIdAndUserId(conversationId, userId);
        }

        // 没有就创建新的私聊会话
        ChatConversation conversation = new ChatConversation();
        conversation.setType(1);
        conversation.setPrivateKey(privateKey);
        conversation.setName(null);
        conversation.setOwnerId(userId);
        conversation.setStatus(1);

        chatMapper.insertChatConversation(conversation);

        Long newConversationId = conversation.getId();

        // 当前用户是 owner，role = 1
        chatMapper.insertConversationMember(newConversationId, userId, 1);

        // 对方是普通成员，role = 2
        chatMapper.insertConversationMember(newConversationId, targetUserId, 2);

        return chatMapper.selectChatRoomByIdAndUserId(newConversationId, userId);
    }

    /**
     * 生成私聊唯一 key，保证 2 和 5 永远生成 2_5
     */
    private String buildPrivateKey(Long userId, Long targetUserId) {
        long min = Math.min(userId, targetUserId);
        long max = Math.max(userId, targetUserId);
        return min + "_" + max;
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

        // 是否阅后即焚：前端传 true 就是 1，否则默认 0
        Integer burnAfterRead = Boolean.TRUE.equals(dto.getBurnAfterRead()) ? 1 : 0;
        message.setBurnAfterRead(burnAfterRead);

        // 新消息默认未焚毁
        message.setBurned(0);

        message.setContent(dto.getContent().trim());
        message.setStatus(1);
        message.setSendTime(LocalDateTime.now());

        chatMapper.insertChatMessage(message);
        // 发送人自己默认已读
        chatMapper.insertMessageReadRecord(
                message.getId(),
                dto.getConversationId(),
                userId
        );

        chatReadBitmapService.markMessageRead(
                message.getId(),
                userId
        );

        // 如果是阅后即焚消息，会话列表最后一条不要展示原文
        String lastMessageContent = burnAfterRead == 1
                ? "[阅后即焚消息]"
                : message.getContent();

        chatMapper.updateConversationLastMessage(
                dto.getConversationId(),
                message.getId(),
                lastMessageContent
        );

        // 给其他成员未读数 +1
        chatMapper.increaseUnreadCount(dto.getConversationId(), userId);

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
    @Override
    public List<PrivateChatUserVO> getPrivateChatUsers(Long userId) {
        return chatMapper.selectPrivateChatUsers(userId);
    }
    @Override
    @Transactional
    public void burnReadMessage(Long userId, Long messageId) {
        if (messageId == null) {
            throw new RuntimeException("消息ID不能为空");
        }

        ChatMessage message = chatMapper.selectChatMessageEntityById(messageId);

        if (message == null) {
            throw new RuntimeException("消息不存在");
        }

        Integer canAccess = chatMapper.countUserCanAccessMessage(messageId, userId);
        if (canAccess == null || canAccess <= 0) {
            throw new RuntimeException("你无权查看该消息");
        }

        if (message.getBurnAfterRead() == null || message.getBurnAfterRead() != 1) {
            throw new RuntimeException("该消息不是阅后即焚消息");
        }

        if (message.getBurned() != null && message.getBurned() == 1) {
            return;
        }

        if (message.getSenderId().equals(userId)) {
            throw new RuntimeException("发送人不能触发自己的阅后即焚消息");
        }

        chatMapper.burnMessageAfterRead(messageId, userId);
    }

    @Override
    public Long getMessageReadCount(Long userId, Long messageId) {
        if (messageId == null) {
            throw new RuntimeException("消息ID不能为空");
        }

        Integer canAccess = chatMapper.countUserCanAccessMessage(messageId, userId);
        if (canAccess == null || canAccess <= 0) {
            throw new RuntimeException("你无权查看该消息的已读信息");
        }

        return chatReadBitmapService.countMessageRead(messageId);
    }

    @Override
    public List<ChatMessageReadUserVO> getMessageReadUsers(Long userId, Long messageId) {
        if (messageId == null) {
            throw new RuntimeException("消息ID不能为空");
        }

        Integer canAccess = chatMapper.countUserCanAccessMessage(messageId, userId);
        if (canAccess == null || canAccess <= 0) {
            throw new RuntimeException("你无权查看该消息的已读用户");
        }

        return chatMapper.selectMessageReadUsers(messageId);
    }
}