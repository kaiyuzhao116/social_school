package com.campusconnect.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campusconnect.dto.*;
import com.campusconnect.entity.*;
import com.campusconnect.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final ConversationMapper conversationMapper;
    private final ConversationMemberMapper memberMapper;
    private final MessageMapper messageMapper;
    private final UserMapper userMapper;

    /**
     * 获取用户的会话列表
     */
    public List<ConversationVO> getConversations(Long userId) {
        // 1. 查询用户参与的所有会话
        List<ConversationMember> memberships = memberMapper.selectList(
                new LambdaQueryWrapper<ConversationMember>()
                        .eq(ConversationMember::getUserId, userId));

        if (memberships.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> conversationIds = memberships.stream()
                .map(ConversationMember::getConversationId)
                .collect(Collectors.toList());

        // 2. 查询会话详情
        List<Conversation> conversations = conversationMapper.selectBatchIds(conversationIds);

        // 3. 转换为 VO
        Map<Long, ConversationMember> membershipMap = memberships.stream()
                .collect(Collectors.toMap(ConversationMember::getConversationId, m -> m));

        return conversations.stream()
                .map(conv -> toConversationVO(conv, membershipMap.get(conv.getId()), userId))
                .sorted((a, b) -> {
                    if (a.getUpdatedAt() == null && b.getUpdatedAt() == null)
                        return 0;
                    if (a.getUpdatedAt() == null)
                        return 1;
                    if (b.getUpdatedAt() == null)
                        return -1;
                    return b.getUpdatedAt().compareTo(a.getUpdatedAt());
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取会话消息
     */
    public Page<MessageVO> getMessages(Long conversationId, Long userId, int page, int size) {
        // 验证用户是否在会话中
        ConversationMember member = memberMapper.selectOne(
                new LambdaQueryWrapper<ConversationMember>()
                        .eq(ConversationMember::getConversationId, conversationId)
                        .eq(ConversationMember::getUserId, userId));

        if (member == null) {
            throw new RuntimeException("无权访问此会话");
        }

        Page<Message> messagePage = messageMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getConversationId, conversationId)
                        .orderByDesc(Message::getCreatedAt));

        // 转换为 VO
        Page<MessageVO> voPage = new Page<>(page, size, messagePage.getTotal());
        List<MessageVO> voList = messagePage.getRecords().stream()
                .map(msg -> toMessageVO(msg, userId))
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    /**
     * 发送消息
     */
    @Transactional
    public MessageVO sendMessage(Long senderId, SendMessageRequest request) {
        Long conversationId = request.getConversationId();

        // 如果没有会话ID，创建私聊会话
        if (conversationId == null && request.getTargetUserId() != null) {
            conversationId = getOrCreatePrivateConversation(senderId, request.getTargetUserId());
        }

        if (conversationId == null) {
            throw new RuntimeException("会话ID或目标用户ID不能都为空");
        }

        // 验证用户是否在会话中
        ConversationMember member = memberMapper.selectOne(
                new LambdaQueryWrapper<ConversationMember>()
                        .eq(ConversationMember::getConversationId, conversationId)
                        .eq(ConversationMember::getUserId, senderId));

        if (member == null) {
            throw new RuntimeException("无权在此会话中发送消息");
        }

        // 创建消息
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setContent(request.getContent());
        message.setType(request.getType() != null ? request.getType() : "TEXT");
        message.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(message);

        // 更新会话时间
        Conversation conv = conversationMapper.selectById(conversationId);
        conv.setUpdatedAt(LocalDateTime.now());
        conversationMapper.updateById(conv);

        // 增加其他成员的未读数
        memberMapper.incrementUnreadCount(conversationId, senderId);

        return toMessageVO(message, senderId);
    }

    /**
     * 标记会话已读
     */
    public void markConversationAsRead(Long conversationId, Long userId) {
        memberMapper.update(null,
                new LambdaUpdateWrapper<ConversationMember>()
                        .eq(ConversationMember::getConversationId, conversationId)
                        .eq(ConversationMember::getUserId, userId)
                        .set(ConversationMember::getUnreadCount, 0)
                        .set(ConversationMember::getLastReadAt, LocalDateTime.now()));
    }

    /**
     * 删除会话（从成员中移除）
     */
    @Transactional
    public void deleteConversation(Long conversationId, Long userId) {
        // 从会话成员中移除
        memberMapper.delete(
                new LambdaQueryWrapper<ConversationMember>()
                        .eq(ConversationMember::getConversationId, conversationId)
                        .eq(ConversationMember::getUserId, userId));

        // 检查是否还有其他成员，如果没有则删除会话
        Integer remainingMembers = memberMapper.getMemberCount(conversationId);
        if (remainingMembers == 0) {
            // 删除所有消息
            messageMapper.delete(
                    new LambdaQueryWrapper<Message>()
                            .eq(Message::getConversationId, conversationId));
            // 删除会话
            conversationMapper.deleteById(conversationId);
        }
    }

    /**
     * 获取未读消息总数
     */
    public Integer getUnreadCount(Long userId) {
        return memberMapper.getTotalUnreadCount(userId);
    }

    /**
     * 创建群聊
     */
    @Transactional
    public ConversationVO createGroup(Long ownerId, CreateGroupRequest request) {
        // 创建会话
        Conversation conv = new Conversation();
        conv.setName(request.getName());
        conv.setType("GROUP");
        conv.setAvatar(request.getAvatar());
        conv.setOwnerId(ownerId);
        conv.setCreatedAt(LocalDateTime.now());
        conv.setUpdatedAt(LocalDateTime.now());
        conversationMapper.insert(conv);

        // 添加群主
        addMember(conv.getId(), ownerId, "OWNER");

        // 添加成员
        if (request.getMemberIds() != null) {
            for (Long memberId : request.getMemberIds()) {
                if (!memberId.equals(ownerId)) {
                    addMember(conv.getId(), memberId, "MEMBER");
                }
            }
        }

        // 获取创建者的成员关系
        ConversationMember membership = memberMapper.selectOne(
                new LambdaQueryWrapper<ConversationMember>()
                        .eq(ConversationMember::getConversationId, conv.getId())
                        .eq(ConversationMember::getUserId, ownerId));

        return toConversationVO(conv, membership, ownerId);
    }

    /**
     * 获取会话详情（包含成员列表）
     */
    public ConversationVO getConversationDetail(Long conversationId, Long userId) {
        // 验证用户是否在会话中
        ConversationMember member = memberMapper.selectOne(
                new LambdaQueryWrapper<ConversationMember>()
                        .eq(ConversationMember::getConversationId, conversationId)
                        .eq(ConversationMember::getUserId, userId));

        if (member == null) {
            throw new RuntimeException("无权访问此会话");
        }

        Conversation conv = conversationMapper.selectById(conversationId);
        if (conv == null) {
            throw new RuntimeException("会话不存在");
        }

        ConversationVO vo = toConversationVO(conv, member, userId);

        // 获取成员列表
        List<ConversationMember> members = memberMapper.selectList(
                new LambdaQueryWrapper<ConversationMember>()
                        .eq(ConversationMember::getConversationId, conversationId));

        List<MemberVO> memberVOs = members.stream()
                .map(this::toMemberVO)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        vo.setMembers(memberVOs);
        vo.setMemberCount(memberVOs.size());

        return vo;
    }

    /**
     * 获取或创建私聊会话
     */
    public Long getOrCreatePrivateConversation(Long userId1, Long userId2) {
        // 查找是否已存在私聊
        Long existingId = conversationMapper.findPrivateConversation(userId1, userId2);
        if (existingId != null) {
            return existingId;
        }

        // 创建新会话
        Conversation conv = new Conversation();
        conv.setType("PRIVATE");
        conv.setCreatedAt(LocalDateTime.now());
        conv.setUpdatedAt(LocalDateTime.now());
        conversationMapper.insert(conv);

        // 添加成员
        addMember(conv.getId(), userId1, "MEMBER");
        addMember(conv.getId(), userId2, "MEMBER");

        return conv.getId();
    }

    private void addMember(Long conversationId, Long userId, String role) {
        ConversationMember member = new ConversationMember();
        member.setConversationId(conversationId);
        member.setUserId(userId);
        member.setRole(role);
        member.setUnreadCount(0);
        member.setJoinedAt(LocalDateTime.now());
        memberMapper.insert(member);
    }

    private ConversationVO toConversationVO(Conversation conv, ConversationMember membership, Long currentUserId) {
        ConversationVO vo = new ConversationVO();
        vo.setId(conv.getId());
        vo.setType(conv.getType());
        vo.setUnreadCount(membership != null ? membership.getUnreadCount() : 0);
        vo.setUpdatedAt(conv.getUpdatedAt());
        vo.setOwnerId(conv.getOwnerId());

        if ("GROUP".equals(conv.getType())) {
            vo.setName(conv.getName());
            vo.setAvatar(conv.getAvatar());
            vo.setAnnouncement(conv.getAnnouncement());
            vo.setMemberCount(memberMapper.getMemberCount(conv.getId()));
        } else {
            // 私聊：获取对方信息
            User target = getOtherUser(conv.getId(), currentUserId);
            if (target != null) {
                vo.setTargetId(target.getId());
                vo.setTargetName(target.getNickname());
                vo.setTargetAvatar(target.getAvatar());
                vo.setName(target.getNickname());
                vo.setAvatar(target.getAvatar());

                // 兼容逻辑：如果没有 verifyType，尝试从 role 获取
                String vType = target.getVerifyType();
                if (vType == null || vType.isEmpty()) {
                    if ("teacher".equalsIgnoreCase(target.getRole()))
                        vType = "TEACHER";
                    else if ("department".equalsIgnoreCase(target.getRole()))
                        vType = "ORG";
                }
                vo.setTargetVerifyType(vType);
            }
        }

        // 获取最后一条消息
        Message lastMsg = messageMapper.selectOne(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getConversationId, conv.getId())
                        .orderByDesc(Message::getCreatedAt)
                        .last("LIMIT 1"));
        if (lastMsg != null) {
            vo.setLastMessage(lastMsg.getContent());
            User sender = userMapper.selectById(lastMsg.getSenderId());
            if (sender != null) {
                vo.setLastSenderName(sender.getNickname());
            }
        }

        return vo;
    }

    private MessageVO toMessageVO(Message msg, Long currentUserId) {
        MessageVO vo = new MessageVO();
        vo.setId(msg.getId());
        vo.setConversationId(msg.getConversationId());
        vo.setSenderId(msg.getSenderId());
        vo.setContent(msg.getContent());
        vo.setType(msg.getType());
        vo.setCreatedAt(msg.getCreatedAt());
        vo.setIsSelf(msg.getSenderId().equals(currentUserId));

        User sender = userMapper.selectById(msg.getSenderId());
        if (sender != null) {
            vo.setSenderName(sender.getNickname());
            vo.setSenderAvatar(sender.getAvatar());
        }

        return vo;
    }

    private MemberVO toMemberVO(ConversationMember member) {
        User user = userMapper.selectById(member.getUserId());
        if (user == null) {
            return null;
        }

        MemberVO vo = new MemberVO();
        vo.setId(user.getId());
        vo.setName(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setRole(member.getRole());
        return vo;
    }

    private User getOtherUser(Long conversationId, Long currentUserId) {
        List<ConversationMember> members = memberMapper.selectList(
                new LambdaQueryWrapper<ConversationMember>()
                        .eq(ConversationMember::getConversationId, conversationId));
        for (ConversationMember m : members) {
            if (!m.getUserId().equals(currentUserId)) {
                return userMapper.selectById(m.getUserId());
            }
        }
        return null;
    }
}
