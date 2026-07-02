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
import java.util.List;

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

        List<GroupBuyMember> members = groupBuyMemberService.lambdaQuery()
                .eq(GroupBuyMember::getGroupBuyId, message.getGroupBuyId())
                .list();

        if (members == null || members.isEmpty()) {
            log.warn("【通知模块】拼团没有成员，跳过通知生成，groupBuyId={}", message.getGroupBuyId());
            return;
        }

        String title = buildTitle(message);
        String content = buildContent(message);

        List<Notification> notifications = new ArrayList<>();

        for (GroupBuyMember member : members) {
            Notification notification = new Notification();

            // 接收者：拼团成员
            notification.setUserId(member.getUserId());

            // 系统通知，发送者可以为空
            notification.setSenderId(null);

            // 类型用 SYSTEM，和你实体注释对应
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
            return "你参与的《" + message.getTitle()
                    + "》已成功成团，当前人数 "
                    + message.getCurrentCount()
                    + " / "
                    + message.getTargetCount()
                    + "，请及时联系队友完成拼单。";
        }

        if ("GROUP_BUY_CANCELLED".equals(message.getEventType())) {
            return "你参与的《" + message.getTitle()
                    + "》已被取消，请留意后续拼团信息。";
        }

        if ("GROUP_BUY_EXPIRED".equals(message.getEventType())) {
            return "你参与的《" + message.getTitle()
                    + "》已过期未成团。";
        }

        return "你参与的《" + message.getTitle() + "》状态发生变化。";
    }
}