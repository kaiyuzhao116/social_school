package com.campusconnect.controller;

import com.campusconnect.common.Result;
import com.campusconnect.dto.CreateGroupRequest;
import com.campusconnect.dto.SendMessageRequest;
import com.campusconnect.security.UserPrincipal;
import com.campusconnect.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * 获取会话列表
     */
    @GetMapping("/conversations")
    public Result<?> getConversations(@AuthenticationPrincipal UserPrincipal principal) {
        return Result.success(messageService.getConversations(principal.getId()));
    }

    /**
     * 获取会话详情（包含成员列表）
     */
    @GetMapping("/conversations/{conversationId}")
    public Result<?> getConversationDetail(
            @PathVariable Long conversationId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return Result.success(messageService.getConversationDetail(conversationId, principal.getId()));
    }

    /**
     * 获取会话消息历史
     */
    @GetMapping("/conversations/{conversationId}/messages")
    public Result<?> getMessages(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @AuthenticationPrincipal UserPrincipal principal) {
        return Result.success(messageService.getMessages(conversationId, principal.getId(), page, size));
    }

    /**
     * 发送消息
     */
    @PostMapping
    public Result<?> sendMessage(
            @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return Result.success(messageService.sendMessage(principal.getId(), request));
    }

    /**
     * 标记会话已读
     */
    @PutMapping("/conversations/{conversationId}/read")
    public Result<?> markAsRead(
            @PathVariable Long conversationId,
            @AuthenticationPrincipal UserPrincipal principal) {
        messageService.markConversationAsRead(conversationId, principal.getId());
        return Result.success();
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/conversations/{conversationId}")
    public Result<?> deleteConversation(
            @PathVariable Long conversationId,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            if (principal == null) {
                return Result.error("未登录");
            }
            System.out.println("[Message] Deleting conversation: " + conversationId + " by user: " + principal.getId());
            messageService.deleteConversation(conversationId, principal.getId());
            System.out.println("[Message] Conversation deleted successfully");
            return Result.success();
        } catch (Exception e) {
            System.err.println("[Message Error] Failed to delete conversation: " + e.getMessage());
            e.printStackTrace();
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取未读消息数
     */
    @GetMapping("/unread-count")
    public Result<?> getUnreadCount(@AuthenticationPrincipal UserPrincipal principal) {
        return Result.success(messageService.getUnreadCount(principal.getId()));
    }

    /**
     * 创建群聊
     */
    @PostMapping("/groups")
    public Result<?> createGroup(
            @RequestBody CreateGroupRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return Result.success(messageService.createGroup(principal.getId(), request));
    }

    /**
     * 获取或创建私聊会话
     */
    @PostMapping("/private/{targetUserId}")
    public Result<?> getOrCreatePrivateConversation(
            @PathVariable Long targetUserId,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            if (principal == null) {
                return Result.error("未登录");
            }
            System.out.println("[Message Debug] Creating private conversation: userId=" + principal.getId()
                    + ", targetId=" + targetUserId);
            Long conversationId = messageService.getOrCreatePrivateConversation(principal.getId(), targetUserId);
            System.out.println("[Message Debug] Conversation ID: " + conversationId);
            return Result.success(messageService.getConversationDetail(conversationId, principal.getId()));
        } catch (Exception e) {
            System.err.println("[Message Error] Failed to create private conversation: " + e.getMessage());
            e.printStackTrace();
            return Result.error("创建会话失败: " + e.getMessage());
        }
    }
}
