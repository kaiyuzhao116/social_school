package com.campusconnect.common.dynamic;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DynamicConfigService {

    private final StringRedisTemplate stringRedisTemplate;

    public static final String CONFIG_HASH_KEY = "campus:dynamic:config";
    public static final String CONFIG_TOPIC = "campus:dynamic:config:update";

    public String getConfig(String key) {
        Object value = stringRedisTemplate.opsForHash().get(CONFIG_HASH_KEY, key);
        return value == null ? null : value.toString();
    }

    public void updateConfig(String key, String value) {
        stringRedisTemplate.opsForHash().put(CONFIG_HASH_KEY, key, value);

        // 发布配置变更消息
        stringRedisTemplate.convertAndSend(CONFIG_TOPIC, key + "=" + value);
    }
}