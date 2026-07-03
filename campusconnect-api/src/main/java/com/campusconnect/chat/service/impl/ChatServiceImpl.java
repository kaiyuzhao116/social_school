package com.campusconnect.chat.service.impl;

import com.campusconnect.chat.dto.ChatSendMessageDTO;
import com.campusconnect.chat.entity.ChatMessage;
import com.campusconnect.chat.mapper.ChatMapper;
import com.campusconnect.chat.service.ChatService;
import com.campusconnect.chat.vo.ChatMessageVO;
import com.campusconnect.chat.vo.ChatRoomVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatMapper chatMapper;

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

        chatMapper.increaseUnreadCount(dto.getConversationId(), userId);

        return chatMapper.selectMessageById(message.getId());
    }
}