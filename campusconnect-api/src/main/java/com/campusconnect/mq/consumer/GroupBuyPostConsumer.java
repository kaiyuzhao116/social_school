package com.campusconnect.mq.consumer;

import com.campusconnect.config.RabbitMQConfig;
import com.campusconnect.entity.Post;
import com.campusconnect.mq.event.GroupBuyEventMessage;
import com.campusconnect.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroupBuyPostConsumer {

    private final PostService postService;

    /**
     * 动态模块消费者：
     * 只监听拼团成功事件，用于自动生成校园动态
     */
    @RabbitListener(queues = RabbitMQConfig.GROUP_BUY_POST_QUEUE)
    public void handleGroupBuySuccess(GroupBuyEventMessage message) {
        log.info("【动态模块】收到拼团成功MQ事件: {}", message);

        if (!"GROUP_BUY_SUCCESS".equals(message.getEventType())) {
            return;
        }

        String content = "🎉 拼团成功！“" + message.getTitle()
                + "” 已成功成团，当前人数 "
                + message.getCurrentCount()
                + " / "
                + message.getTargetCount()
                + "，大家可以联系队友完成拼单啦！";

        Post post = new Post();

        // 用拼团发起人作为动态发布者
        post.setUserId(message.getInitiatorId());

        post.setContent(content);

        // 不带图片
        post.setImages(null);

        // tags 字段你数据库里是 JSON 字符串格式
        post.setTags("[\"校园拼团\",\"拼团成功\"]");

        post.setStatus("PUBLISHED");

        post.setIsPinned(false);
        post.setIsAnonymous(false);

        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setShareCount(0);
        post.setViewCount(0);

        // AI审核字段，系统自动生成的动态默认安全
        post.setAiSafe(true);
        post.setAiReason("系统拼团成团自动发布");
        post.setAiConfidence(100);

        postService.save(post);

        log.info("【动态模块】已自动发布拼团成功动态，postId={}, content={}", post.getId(), content);
    }
}