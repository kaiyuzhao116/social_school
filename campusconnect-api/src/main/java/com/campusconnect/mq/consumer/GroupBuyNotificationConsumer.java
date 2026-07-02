package com.campusconnect.mq.consumer;

import com.campusconnect.config.RabbitMQConfig;
import com.campusconnect.entity.GroupBuyMember;
import com.campusconnect.entity.Notification;
import com.campusconnect.mq.event.GroupBuyEventMessage;
import com.campusconnect.service.GroupBuyMemberService;
import com.campusconnect.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroupBuyNotificationConsumer {

    private final NotificationService notificationService;
    private final GroupBuyMemberService groupBuyMemberService;

    /**
     * 通知模块消费者：
     * 监听拼团成功、取消、过期事件，并写入 notification 表
     */
    @RabbitListener(queues = RabbitMQConfig.GROUP_BUY_NOTIFICATION_QUEUE)
    public void handleGroupBuyEvent(GroupBuyEventMessage message) {
        log.info("【通知模块】收到拼团MQ事件: {}", message);

        if (message == null || message.getGroupBuyId() == null) {
            log.warn("【通知模块】消息为空，跳过通知生成");
            return;
        }

        List<GroupBuyMember> members = groupBuyMemberService.lambdaQuery()
                .eq(GroupBuyMember::getGroupBuyId, message.getGroupBuyId())
                .list();

        /**
         * 通知接收人：
         * 1. 拼团成员
         * 2. 拼团发起人
         *
         * 用 Set 去重，避免发起人同时也是成员时重复通知。
         */
        Set<Long> receiverIds = new LinkedHashSet<>();

        if (members != null && !members.isEmpty()) {
            for (GroupBuyMember member : members) {
                if (member.getUserId() != null) {
                    receiverIds.add(member.getUserId());
                }
            }
        }

        // 即使没人参加，也要通知发起人，比如拼团过期、拼团取消
        if (message.getInitiatorId() != null) {
            receiverIds.add(message.getInitiatorId());
        }

        if (receiverIds.isEmpty()) {
            log.warn("【通知模块】没有可通知用户，跳过通知生成，groupBuyId={}",
                    message.getGroupBuyId());
            return;
        }

        String title = buildTitle(message);
        String content = buildContent(message);

        List<Notification> notifications = new ArrayList<>();

        for (Long receiverId : receiverIds) {
            Notification notification = new Notification();

            // 接收者：拼团成员或发起人
            notification.setUserId(receiverId);

            // 系统通知，发送者为空
            notification.setSenderId(null);

            // 类型：系统通知
            notification.setType("SYSTEM");

            notification.setTitle(title);
            notification.setContent(content);

            // 关联对象：拼团ID
            notification.setTargetId(message.getGroupBuyId());
            notification.setTargetType("GROUP_BUY");

            // 未读
            notification.setIsRead(false);

            notifications.add(notification);
        }

        notificationService.saveBatch(notifications);

        log.info("【通知模块】已生成拼团通知，groupBuyId={}, 通知人数={}",
                message.getGroupBuyId(),
                notifications.size()
        );
    }

    private String buildTitle(GroupBuyEventMessage message) {
        if ("GROUP_BUY_SUCCESS".equals(message.getEventType())) {
            return "拼团已成团";
        }

        if ("GROUP_BUY_CANCELLED".equals(message.getEventType())) {
            return "拼团已取消";
        }

        if ("GROUP_BUY_EXPIRED".equals(message.getEventType())) {
            return "拼团已过期";
        }

        return "拼团状态更新";
    }

    private String buildContent(GroupBuyEventMessage message) {
        if ("GROUP_BUY_SUCCESS".equals(message.getEventType())) {
            return "你参与或发起的《" + message.getTitle()
                    + "》已成功成团，当前人数 "
                    + message.getCurrentCount()
                    + " / "
                    + message.getTargetCount()
                    + "，请及时联系队友完成拼单。";
        }

        if ("GROUP_BUY_CANCELLED".equals(message.getEventType())) {
            return "你参与或发起的《" + message.getTitle()
                    + "》已被取消，请留意后续拼团信息。";
        }

        if ("GROUP_BUY_EXPIRED".equals(message.getEventType())) {
            return "你参与或发起的《" + message.getTitle()
                    + "》已过期未成团，系统已自动结束该拼团。";
        }

        return "你参与或发起的《" + message.getTitle() + "》状态发生变化。";
    }
}