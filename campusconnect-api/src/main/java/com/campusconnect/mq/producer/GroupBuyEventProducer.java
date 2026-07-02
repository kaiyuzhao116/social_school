package com.campusconnect.mq.producer;

import com.campusconnect.config.RabbitMQConfig;
import com.campusconnect.mq.event.GroupBuyEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroupBuyEventProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送拼团成功事件
     */
    public void sendSuccessEvent(GroupBuyEventMessage message) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.CAMPUS_EVENT_EXCHANGE,
                RabbitMQConfig.GROUP_BUY_SUCCESS_KEY,
                message
        );

        log.info("发送拼团成功MQ事件: {}", message);
    }

    /**
     * 发送拼团取消事件
     */
    public void sendCancelledEvent(GroupBuyEventMessage message) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.CAMPUS_EVENT_EXCHANGE,
                RabbitMQConfig.GROUP_BUY_CANCELLED_KEY,
                message
        );

        log.info("发送拼团取消MQ事件: {}", message);
    }

    /**
     * 发送拼团过期事件
     */
    public void sendExpiredEvent(GroupBuyEventMessage message) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.CAMPUS_EVENT_EXCHANGE,
                RabbitMQConfig.GROUP_BUY_EXPIRED_KEY,
                message
        );

        log.info("发送拼团过期MQ事件: {}", message);
    }
}