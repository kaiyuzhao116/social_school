package com.campusconnect.system;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class VisitMetricsFilter extends OncePerRequestFilter {

    private final StringRedisTemplate stringRedisTemplate;
    /**
     * 这个是小时格式化器。
     * 比如现在时间是：
     * 2026-07-05 03:25:30  格式化后就是：2026070503
     * 这个格式会用来拼 Redis key。
     */
    private static final DateTimeFormatter HOUR_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHH");
    //request：当前请求对象，可以拿 URI、请求方法、IP 等
    //response：当前响应对象
    //filterChain：过滤器链，用来继续放行请求
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            recordVisit(request);
        } catch (Exception ignored) {
            // 访问统计失败不能影响正常业务
        }

        filterChain.doFilter(request, response);
    }

    private void recordVisit(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        // 不统计 OPTIONS、错误页、WebSocket、管理看板自身接口，避免自己刷新自己导致数据虚高
       // 浏览器跨域时会自动发 OPTIONS 预检请求 2不然系统报错时也会刷访问量。3因为 WebSocket 是长连接，不是普通接口访问。
        //后台数据看板自己的接口不统计。 动态配置接口也不统计。
        //比如你后台点“开启限流”“关闭限流”，这种不算普通用户访问。

        if ("OPTIONS".equalsIgnoreCase(method)
                || uri.contains("/error")
                || uri.contains("/ws/")
                || uri.contains("/admin/dashboard")
                || uri.contains("/admin/dynamic-config")) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        String hour = now.format(HOUR_FORMATTER);//注意：这里只精确到小时，不管分钟和秒。
        //campus:metrics:visit:hour:2026070503
        //这个 key 的意思是：
        //2026 年 07 月 05 日 03 点这一小时的访问量
        String hourKey = "campus:metrics:visit:hour:" + hour;

        stringRedisTemplate.opsForValue().increment(hourKey);
        stringRedisTemplate.expire(hourKey, Duration.ofDays(1));//为什么要设置过期？
        //因为每天会产生 24 个小时 key。
        //如果不删，Redis 里会越来越多。
        //你后台只看最近 7 小时，所以保留 1 天足够了。
    }
}