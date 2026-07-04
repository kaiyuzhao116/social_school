package com.campusconnect.common.dynamic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicConfigSubscriber implements MessageListener {

    private final DynamicConfigRegistry dynamicConfigRegistry;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String body = new String(message.getBody(), StandardCharsets.UTF_8);

        log.info("【动态配置】收到 Redis 配置变更消息：{}", body);

        if (!body.contains("=")) {
            return;
        }

        String[] parts = body.split("=", 2);
        String key = parts[0];
        String value = parts[1];

        dynamicConfigRegistry.updateValue(key, value);
    }
}