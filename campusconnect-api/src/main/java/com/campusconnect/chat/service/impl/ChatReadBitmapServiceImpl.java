package com.campusconnect.chat.service.impl;

import com.campusconnect.chat.service.ChatReadBitmapService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class ChatReadBitmapServiceImpl implements ChatReadBitmapService {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String MESSAGE_READ_KEY_PREFIX = "chat:message:read:";

    private String buildMessageReadKey(Long messageId) {
        return MESSAGE_READ_KEY_PREFIX + messageId;
    }

    @Override
    public void markMessageRead(Long messageId, Long userId) {
        if (messageId == null || userId == null) {
            return;
        }

        String key = buildMessageReadKey(messageId);

        stringRedisTemplate.opsForValue().setBit(
                key,
                userId,
                true
        );

        // Redis 只是加速统计，MySQL 才是最终记录，所以这里设置一个过期时间即可
        stringRedisTemplate.expire(key, Duration.ofDays(30));
    }

    @Override
    public Boolean isMessageRead(Long messageId, Long userId) {
        if (messageId == null || userId == null) {
            return false;
        }

        String key = buildMessageReadKey(messageId);

        Boolean result = stringRedisTemplate.opsForValue().getBit(
                key,
                userId
        );

        return Boolean.TRUE.equals(result);
    }

    @Override
    public Long countMessageRead(Long messageId) {
        if (messageId == null) {
            return 0L;
        }

        String key = buildMessageReadKey(messageId);

        Long count = stringRedisTemplate.execute((RedisCallback<Long>) connection ->
                connection.bitCount(key.getBytes(StandardCharsets.UTF_8))
        );

        return count == null ? 0L : count;
    }
}