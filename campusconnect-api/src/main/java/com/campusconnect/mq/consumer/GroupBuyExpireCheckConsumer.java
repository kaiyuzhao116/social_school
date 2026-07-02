package com.campusconnect.mq.consumer;

import com.campusconnect.config.RabbitMQConfig;
import com.campusconnect.entity.GroupBuy;
import com.campusconnect.mq.event.GroupBuyEventMessage;
import com.campusconnect.mq.event.GroupBuyExpireCheckMessage;
import com.campusconnect.mq.producer.GroupBuyEventProducer;
import com.campusconnect.service.GroupBuyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 拼团过期检查消费者
 *
 * 监听 RabbitMQ 延迟队列 TTL 到期后转发过来的检查消息。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GroupBuyExpireCheckConsumer {

    private final GroupBuyService groupBuyService;
    private final GroupBuyEventProducer groupBuyEventProducer;

    /**
     * 监听过期检查队列
     */
    @RabbitListener(queues = RabbitMQConfig.GROUP_BUY_EXPIRE_CHECK_QUEUE)
    public void handleExpireCheck(GroupBuyExpireCheckMessage message) {
        log.info("【过期补偿模块】收到拼团过期检查消息: {}", message);

        if (message == null || message.getGroupBuyId() == null) {
            log.warn("【过期补偿模块】消息为空，跳过处理");
            return;
        }

        GroupBuy groupBuy = groupBuyService.getById(message.getGroupBuyId());

        if (groupBuy == null) {
            log.warn("【过期补偿模块】拼团不存在，groupBuyId={}", message.getGroupBuyId());
            return;
        }

        if (!"GROUPING".equals(groupBuy.getStatus())) {
            log.info("【过期补偿模块】拼团状态不是拼团中，跳过处理，groupBuyId={}, status={}",
                    groupBuy.getId(),
                    groupBuy.getStatus()
            );
            return;
        }

        if (groupBuy.getDeadline() == null) {
            log.warn("【过期补偿模块】拼团截止时间为空，跳过处理，groupBuyId={}", groupBuy.getId());
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(groupBuy.getDeadline())) {
            log.info("【过期补偿模块】拼团还未到截止时间，跳过处理，groupBuyId={}, deadline={}",
                    groupBuy.getId(),
                    groupBuy.getDeadline()
            );
            return;
        }

        boolean updated = groupBuyService.lambdaUpdate()
                .eq(GroupBuy::getId, groupBuy.getId())
                .eq(GroupBuy::getStatus, "GROUPING")
                .set(GroupBuy::getStatus, "EXPIRED")
                .update();

        if (!updated) {
            log.warn("【过期补偿模块】拼团过期状态更新失败，可能已被其他流程处理，groupBuyId={}", groupBuy.getId());
            return;
        }

        log.info("【过期补偿模块】拼团已自动过期，groupBuyId={}, title={}",
                groupBuy.getId(),
                groupBuy.getTitle()
        );

        groupBuyEventProducer.sendExpiredEvent(
                GroupBuyEventMessage.builder()
                        .groupBuyId(groupBuy.getId())
                        .title(groupBuy.getTitle())
                        .initiatorId(groupBuy.getInitiatorId())
                        .eventType("GROUP_BUY_EXPIRED")
                        .status("EXPIRED")
                        .currentCount(groupBuy.getCurrentCount())
                        .targetCount(groupBuy.getTargetCount())
                        .eventTime(now)
                        .build()
        );
    }
}