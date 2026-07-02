package com.campusconnect.mq.consumer;

import com.campusconnect.config.RabbitMQConfig;
import com.campusconnect.mq.event.GroupBuyEventMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GroupBuyStatisticsConsumer {

    /**
     * 统计模块消费者：
     * 监听所有拼团事件，用于后台数据看板统计
     */
    @RabbitListener(queues = RabbitMQConfig.GROUP_BUY_STATISTICS_QUEUE)
    public void handleGroupBuyEvent(GroupBuyEventMessage message) {
        log.info("【统计模块】收到拼团MQ事件: {}", message);

        if ("GROUP_BUY_SUCCESS".equals(message.getEventType())) {
            log.info("【统计模块】今日成团数 +1，拼团ID: {}", message.getGroupBuyId());
        }

        if ("GROUP_BUY_CANCELLED".equals(message.getEventType())) {
            log.info("【统计模块】今日取消拼团数 +1，拼团ID: {}", message.getGroupBuyId());
        }

        if ("GROUP_BUY_EXPIRED".equals(message.getEventType())) {
            log.info("【统计模块】今日过期拼团数 +1，拼团ID: {}", message.getGroupBuyId());
        }
    }
}