package com.campusconnect.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步线程池配置
 *
 * 用于学生拼团首页聚合查询：
 * 1. 查询拼团列表
 * 2. 查询当前用户已参加的拼团
 * 3. 查询当前用户发起的拼团
 * 4. 查询拼团统计数据
 */
@Configuration
@EnableAsync
public class AsyncThreadPoolConfig {

    /**
     * 学生拼团模块专用查询线程池
     */
    @Bean("groupBuyQueryExecutor")
    public Executor groupBuyQueryExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数
        executor.setCorePoolSize(4);

        // 最大线程数
        executor.setMaxPoolSize(8);

        // 队列容量
        executor.setQueueCapacity(100);

        // 线程名前缀，方便看日志
        executor.setThreadNamePrefix("group-buy-query-");

        // 队列满了之后，由当前请求线程自己执行，避免任务直接丢失
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();
        return executor;
    }
}