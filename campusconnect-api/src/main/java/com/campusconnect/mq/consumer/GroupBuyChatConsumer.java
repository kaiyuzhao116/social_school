package com.campusconnect.mq.consumer;

import com.campusconnect.chat.service.ChatService;
import com.campusconnect.config.RabbitMQConfig;
import com.campusconnect.entity.GroupBuyMember;
import com.campusconnect.mq.event.GroupBuyEventMessage;
import com.campusconnect.service.GroupBuyMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroupBuyChatConsumer {

    private final GroupBuyMemberService groupBuyMemberService;
    private final ChatService chatService;

    /**
     * 拼团成功后自动创建群聊campusconnect
     */
    @RabbitListener(queues = RabbitMQConfig.GROUP_BUY_CHAT_QUEUE)
    public void handleGroupBuySuccess(GroupBuyEventMessage message) {
        log.info("【拼团群聊模块】收到拼团MQ事件: {}", message);

        if (message == null || message.getGroupBuyId() == null) {
            log.warn("【拼团群聊模块】消息为空，跳过创建群聊");
            return;
        }

        if (!"GROUP_BUY_SUCCESS".equals(message.getEventType())) {
            return;
        }

        List<GroupBuyMember> members = groupBuyMemberService.lambdaQuery()
                .eq(GroupBuyMember::getGroupBuyId, message.getGroupBuyId())
                .list();

        Set<Long> userIds = new LinkedHashSet<>();

        if (message.getInitiatorId() != null) {
            userIds.add(message.getInitiatorId());
        }
//问题是：发起人很可能也在 group_buy_member 表里。
//
//比如拼团：
//
//发起人：用户 1
//参与成员表：
//用户 1
//用户 2
//用户 3
//
//如果不用 Set，你最后插入群成员的时候就会变成：
//
//用户 1 作为群主插入一次
//用户 1 又作为普通成员插入一次
//用户 2 插入一次
//用户 3 插入一次
        if (members != null) {
            for (GroupBuyMember member : members) {
                if (member.getUserId() != null) {
                    userIds.add(member.getUserId());
                }
            }
        }

        if (userIds.isEmpty()) {
            log.warn("【拼团群聊模块】没有可加入群聊的用户，groupBuyId={}", message.getGroupBuyId());
            return;
        }

        Long conversationId = chatService.createGroupConversation(
                message.getInitiatorId(),
                "拼团群聊-" + message.getTitle(),
                userIds
        );

        log.info("【拼团群聊模块】拼团成功后已自动创建群聊，groupBuyId={}, conversationId={}, 成员数={}",
                message.getGroupBuyId(),
                conversationId,
                userIds.size()
        );
    }
}