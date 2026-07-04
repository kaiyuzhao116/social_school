package com.campusconnect.system;

import com.campusconnect.common.dynamic.DccValue;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TrafficControlService {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 是否开启流控：1 开启，0 关闭
     */
    @DccValue("traffic.enabled:1")
    private Integer trafficEnabled;

    /**
     * 每个接口每秒最大访问次数
     */
    @DccValue("traffic.qps:30")
    private Integer trafficQps;

    /**
     * 是否开启全站降级：1 开启，0 关闭
     */
    @DccValue("traffic.degrade:0")
    private Integer trafficDegrade;

    public boolean needReject(String uri) {
        if (trafficEnabled == null || trafficEnabled == 0) {
            return false;
        }

        if (trafficDegrade != null && trafficDegrade == 1) {
            return true;
        }

        int qps = trafficQps == null ? 30 : trafficQps;

        long second = System.currentTimeMillis() / 1000;
        String key = "campus:traffic:" + uri + ":" + second;

        Long count = stringRedisTemplate.opsForValue().increment(key);

        if (count != null && count == 1L) {
            stringRedisTemplate.expire(key, Duration.ofSeconds(2));
        }

        return count != null && count > qps;
    }
}