package com.campusconnect.chat.mq;

import com.campusconnect.chat.service.ChatPushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageConsumer {

    private final ChatPushService chatPushService;

    @RabbitListener(queues = ChatMqConfig.CHAT_PUSH_QUEUE)
    public void handleChatMessage(ChatMessageEvent event) {
        log.info("收到聊天消息MQ事件：{}", event);

        chatPushService.pushMessage(
                event.getConversationId(),
                event.getSenderId(),
                event.getMessageId()
        );
    }
}