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

    /**
     * 拼团过期检查路由键
     * 延迟消息 TTL 到期后，会通过死信交换机转成这个 routing key
     */
    public static final String GROUP_BUY_EXPIRE_CHECK_KEY = "groupbuy.expire.check";

    /**
     * 拼团过期延迟队列
     * 创建拼团时，先把过期检查消息发送到这里
     */
    public static final String GROUP_BUY_EXPIRE_DELAY_QUEUE = "groupbuy.expire.delay.queue";

    /**
     * 拼团过期检查队列
     * 延迟队列中的消息 TTL 到期后，会死信转发到这个队列
     */
    public static final String GROUP_BUY_EXPIRE_CHECK_QUEUE = "groupbuy.expire.check.queue";

    /**
     * 拼团过期延迟队列
     *
     * 消息进入这个队列后不会被消费者直接消费。
     * 等消息 TTL 到期后，会通过死信交换机转发到过期检查队列。
     */

    public static final String GROUP_BUY_CHAT_QUEUE = "group.buy.chat.queue";

    @Bean
    public Queue groupBuyChatQueue() {
        return QueueBuilder.durable(GROUP_BUY_CHAT_QUEUE).build();
    }
    @Bean
    public Queue groupBuyExpireDelayQueue() {
        return QueueBuilder.durable(GROUP_BUY_EXPIRE_DELAY_QUEUE)
                .withArgument("x-dead-letter-exchange", CAMPUS_EVENT_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", GROUP_BUY_EXPIRE_CHECK_KEY)
                .build();
    }

    /**
     * 拼团过期检查队列
     *
     * 延迟消息到期后，会进入这个队列。
     * 后面我们会写消费者监听这个队列，检查拼团是否真的过期。
     */
    @Bean
    public Queue groupBuyExpireCheckQueue() {
        return new Queue(GROUP_BUY_EXPIRE_CHECK_QUEUE, true);
    }
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
     * 群聊模块只监听拼团成功事件
     * 拼团成功后自动创建群聊
     */
    @Bean
    public Binding groupBuyChatBinding(
            Queue groupBuyChatQueue,
            TopicExchange campusEventExchange
    ) {
        return BindingBuilder
                .bind(groupBuyChatQueue)
                .to(campusEventExchange)
                .with(GROUP_BUY_SUCCESS_KEY);
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
    /**
     * 绑定过期检查队列
     *
     * 延迟队列中的消息 TTL 到期后，会以 groupbuy.expire.check 路由键
     * 转发到 campus.event.exchange，再进入这个队列。
     */
    @Bean
    public Binding groupBuyExpireCheckBinding(Queue groupBuyExpireCheckQueue, TopicExchange campusEventExchange) {
        return BindingBuilder.bind(groupBuyExpireCheckQueue)
                .to(campusEventExchange)
                .with(GROUP_BUY_EXPIRE_CHECK_KEY);
    }
}