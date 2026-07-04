package com.campusconnect.chat.service;

import com.campusconnect.chat.dto.ChatSendMessageDTO;
import com.campusconnect.chat.vo.ChatMessageReadUserVO;
import com.campusconnect.chat.vo.ChatMessageVO;
import com.campusconnect.chat.vo.ChatRoomVO;
import com.campusconnect.chat.vo.PrivateChatUserVO;

import java.util.List;

public interface ChatService {

    Long createGroupConversation(
            Long ownerId,
            String name,
            java.util.Collection<Long> memberIds
    );

    /**
     * 标记会话已读
     */
    void markConversationRead(Long userId, Long conversationId);
    /**
     * 获取当前用户的聊天室列表
     */
    List<ChatRoomVO> getChatRooms(Long userId);
    Long getMessageReadCount(Long userId, Long messageId);

    List<ChatMessageReadUserVO> getMessageReadUsers(Long userId, Long messageId);
    /**
     * 获取某个聊天室的历史消息
     */
    List<ChatMessageVO> getChatMessages(Long conversationId);
    void burnReadMessage(Long userId, Long messageId);
    /**
     * 发送聊天消息
     */
    ChatMessageVO sendMessage(Long userId, ChatSendMessageDTO dto);
    List<PrivateChatUserVO> getPrivateChatUsers(Long userId);
    ChatRoomVO createOrGetPrivateConversation(Long userId, Long targetUserId);
}