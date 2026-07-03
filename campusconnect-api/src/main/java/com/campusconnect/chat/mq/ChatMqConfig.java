package com.campusconnect.chat.mq;

import org.springframework.amqp.core.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatMqConfig {

    public static final String CHAT_EXCHANGE = "chat.message.exchange";

    public static final String CHAT_PUSH_QUEUE = "chat.message.push.queue";

    public static final String CHAT_ROUTING_KEY = "chat.message.created";

    @Bean
    public DirectExchange chatExchange() {
        return new DirectExchange(CHAT_EXCHANGE, true, false);
    }

    @Bean
    public Queue chatPushQueue() {
        return QueueBuilder.durable(CHAT_PUSH_QUEUE).build();
    }

    @Bean
    public Binding chatPushBinding() {
        return BindingBuilder
                .bind(chatPushQueue())
                .to(chatExchange())
                .with(CHAT_ROUTING_KEY);
    }


}