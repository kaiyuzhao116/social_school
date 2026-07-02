package com.campusconnect.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
@Configuration
@EnableRabbit
public class RabbitMQConfig {

    /**
     * 统一校园事件交换机
     */
    public static final String CAMPUS_EVENT_EXCHANGE = "campus.event.exchange";

    /**
     * 拼团事件 routing key
     */
    public static final String GROUP_BUY_SUCCESS_KEY = "groupbuy.success";
    public static final String GROUP_BUY_CANCELLED_KEY = "groupbuy.cancelled";
    public static final String GROUP_BUY_EXPIRED_KEY = "groupbuy.expired";

    /**
     * 通知模块队列：监听拼团成功、取消、过期
     */
    public static final String GROUP_BUY_NOTIFICATION_QUEUE = "groupbuy.notification.queue";

    /**
     * 动态模块队列：主要监听拼团成功，自动生成动态
     */
    public static final String GROUP_BUY_POST_QUEUE = "groupbuy.post.queue";

    /**
     * 统计模块队列：监听所有拼团事件，用于统计后台数据
     */
    public static final String GROUP_BUY_STATISTICS_QUEUE = "groupbuy.statistics.queue";

    @Bean
    public TopicExchange campusEventExchange() {
        return new TopicExchange(CAMPUS_EVENT_EXCHANGE, true, false);
    }

    @Bean
    public Queue groupBuyNotificationQueue() {
        return new Queue(GROUP_BUY_NOTIFICATION_QUEUE, true);
    }

    @Bean
    public Queue groupBuyPostQueue() {
        return new Queue(GROUP_BUY_POST_QUEUE, true);
    }

    @Bean
    public Queue groupBuyStatisticsQueue() {
        return new Queue(GROUP_BUY_STATISTICS_QUEUE, true);
    }

    /**
     * 通知模块监听所有拼团事件
     * groupbuy.success / groupbuy.cancelled / groupbuy.expired
     */
    @Bean
    public Binding groupBuyNotificationBinding(
            Queue groupBuyNotificationQueue,
            TopicExchange campusEventExchange
    ) {
        return BindingBuilder
                .bind(groupBuyNotificationQueue)
                .to(campusEventExchange)
                .with("groupbuy.*");
    }

    /**
     * 动态模块只监听拼团成功事件
     */
    @Bean
    public Binding groupBuyPostBinding(
            Queue groupBuyPostQueue,
            TopicExchange campusEventExchange
    ) {
        return BindingBuilder
                .bind(groupBuyPostQueue)
                .to(campusEventExchange)
                .with(GROUP_BUY_SUCCESS_KEY);
    }

    /**
     * 统计模块监听所有拼团事件
     */
    @Bean
    public Binding groupBuyStatisticsBinding(
            Queue groupBuyStatisticsQueue,
            TopicExchange campusEventExchange
    ) {
        return BindingBuilder
                .bind(groupBuyStatisticsQueue)
                .to(campusEventExchange)
                .with("groupbuy.*");
    }

    /**
     * JSON 消息转换器，后面发送对象消息用
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        rabbitAdmin.setAutoStartup(true);
        return rabbitAdmin;
    }

    @Bean
    public ApplicationRunner rabbitMqInitializer(RabbitAdmin rabbitAdmin) {
        return args -> rabbitAdmin.initialize();
    }
}