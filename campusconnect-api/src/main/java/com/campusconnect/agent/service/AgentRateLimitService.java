package com.campusconnect.agent.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AgentRateLimitService {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 简单 Redis 限流
     *
     * @param key 限流 key，比如 IP 或用户 ID
     * @param limit 时间窗口内允许次数
     * @param seconds 时间窗口秒数
     */
    public void checkLimit(String key, int limit, int seconds) {
        Long count = stringRedisTemplate.opsForValue().increment(key);

        if (count != null && count == 1) {
            stringRedisTemplate.expire(key, Duration.ofSeconds(seconds));
        }

        if (count != null && count > limit) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "AI 生成太频繁，请稍后再试"
            );
        }
    }
}