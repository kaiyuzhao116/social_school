package com.campusconnect.mq.producer;

import com.campusconnect.config.RabbitMQConfig;
import com.campusconnect.mq.event.GroupBuyExpireCheckMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroupBuyExpireProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送拼团过期检查延迟消息
     *
     * 注意：
     * 这个消息不是“拼团已过期事件”，只是到点后提醒系统检查。
     */
    public void sendExpireCheckMessage(Long groupBuyId, String title, LocalDateTime deadline) {
        if (groupBuyId == null || deadline == null) {
            log.warn("发送拼团过期检查消息失败，参数为空，groupBuyId={}, deadline={}", groupBuyId, deadline);
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        GroupBuyExpireCheckMessage message = GroupBuyExpireCheckMessage.builder()
                .groupBuyId(groupBuyId)
                .title(title)
                .deadline(deadline)
                .createTime(now)
                .build();

        long ttlMillis = Duration.between(now, deadline).toMillis();

        // 如果截止时间已经到了，直接发送到过期检查队列
        if (ttlMillis <= 0) {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.CAMPUS_EVENT_EXCHANGE,
                    RabbitMQConfig.GROUP_BUY_EXPIRE_CHECK_KEY,
                    message
            );

            log.info("拼团已到截止时间，直接发送过期检查消息：groupBuyId={}, title={}",
                    groupBuyId,
                    title
            );
            return;
        }

        // 发送到延迟队列，设置每条消息自己的 TTL
        rabbitTemplate.convertAndSend(
                "",
                RabbitMQConfig.GROUP_BUY_EXPIRE_DELAY_QUEUE,
                message,
                msg -> {
                    msg.getMessageProperties().setExpiration(String.valueOf(ttlMillis));
                    return msg;
                }
        );

        log.info("发送拼团过期检查延迟消息：groupBuyId={}, title={}, ttl={}ms, deadline={}",
                groupBuyId,
                title,
                ttlMillis,
                deadline
        );
    }
}