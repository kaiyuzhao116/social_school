package com.campusconnect.chat.controller;

import com.campusconnect.chat.dto.ChatSendMessageDTO;
import com.campusconnect.chat.dto.CreatePrivateConversationDTO;
import com.campusconnect.chat.service.ChatService;
import com.campusconnect.chat.vo.ChatMessageVO;
import com.campusconnect.chat.vo.ChatRoomVO;
import com.campusconnect.common.Result;
import com.campusconnect.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    /**
     * 标记会话已读
     */
    @PutMapping("/conversations/{conversationId}/read")
    public Result<Void> markConversationRead(
            @PathVariable Long conversationId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long currentUserId = getCurrentUserId(principal);

        chatService.markConversationRead(currentUserId, conversationId);
        return Result.success();
    }
    /**
     * 获取当前用户的聊天室列表
     */
    @GetMapping("/conversations")
    public Result<List<ChatRoomVO>> getChatRooms(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long currentUserId = getCurrentUserId(principal);

        List<ChatRoomVO> rooms = chatService.getChatRooms(currentUserId);
        return Result.success(rooms);
    }

    /**
     * 获取某个聊天室的历史消息
     */
    @GetMapping("/conversations/{conversationId}/messages")
    public Result<List<ChatMessageVO>> getChatMessages(
            @PathVariable Long conversationId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long currentUserId = getCurrentUserId(principal);

        // 目前先不校验成员关系，下一步再加
        List<ChatMessageVO> messages = chatService.getChatMessages(conversationId);
        return Result.success(messages);
    }

    /**
     * 发送聊天消息
     */
    @PostMapping("/messages")
    public Result<ChatMessageVO> sendMessage(
            @RequestBody ChatSendMessageDTO dto,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long currentUserId = getCurrentUserId(principal);

        ChatMessageVO message = chatService.sendMessage(currentUserId, dto);
        return Result.success(message);
    }
    /**
     * 创建或获取一对一私聊会话
     */
    @PostMapping("/private/conversations")
    public Result<?> createPrivateConversation(
            @RequestBody CreatePrivateConversationDTO dto,
            @AuthenticationPrincipal UserPrincipal principal) {

        Long currentUserId = getCurrentUserId(principal);

        return Result.success(
                chatService.createOrGetPrivateConversation(currentUserId, dto.getTargetUserId())
        );
    }
    /**
     * 获取当前登录用户ID
     * 
     * 先保留兜底 1L，方便你直接浏览器测试接口。
     * 等前端 token 全部正常后，可以把兜底删掉。
     */
    private Long getCurrentUserId(UserPrincipal principal) {
        if (principal == null) {
            throw new RuntimeException("用户未登录，请重新登录");
        }
        return principal.getId();
    }

    /**
     * 查询可私聊用户列表
     */
    @GetMapping("/private/users")
    public Result<?> getPrivateChatUsers(@AuthenticationPrincipal UserPrincipal principal) {
        Long currentUserId = getCurrentUserId(principal);

        return Result.success(chatService.getPrivateChatUsers(currentUserId));
    }
    /**
     * 阅读阅后即焚消息，并触发焚毁
     */
    @PutMapping("/messages/{messageId}/burn-read")
    public Result<?> burnReadMessage(
            @PathVariable Long messageId,
            @AuthenticationPrincipal UserPrincipal principal) {

        Long currentUserId = getCurrentUserId(principal);

        chatService.burnReadMessage(currentUserId, messageId);

        return Result.success();
    }
}