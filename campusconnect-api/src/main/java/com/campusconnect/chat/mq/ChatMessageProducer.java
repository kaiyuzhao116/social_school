package com.campusconnect.chat.mq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendMessageCreated(ChatMessageEvent event) {
        rabbitTemplate.convertAndSend(
                ChatMqConfig.CHAT_EXCHANGE,
                ChatMqConfig.CHAT_ROUTING_KEY,
                event
        );

        log.info("发送聊天消息MQ事件成功：{}", event);
    }
}