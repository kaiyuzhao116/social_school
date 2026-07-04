package com.campusconnect.common.dynamic;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
@RequiredArgsConstructor
public class DynamicConfigRedisConfig {

    private final MessageListener dynamicConfigSubscriber;

    @Bean
    public RedisMessageListenerContainer dynamicConfigRedisMessageListenerContainer(
            RedisConnectionFactory connectionFactory
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        container.addMessageListener(
                dynamicConfigSubscriber,
                new PatternTopic(DynamicConfigService.CONFIG_TOPIC)
        );

        return container;
    }
}