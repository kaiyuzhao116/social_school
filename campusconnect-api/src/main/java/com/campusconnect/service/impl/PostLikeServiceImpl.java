package com.campusconnect.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusconnect.entity.Post;
import com.campusconnect.entity.PostLike;
import com.campusconnect.entity.User;
import com.campusconnect.mapper.PostLikeMapper;
import com.campusconnect.service.NotificationService;
import com.campusconnect.service.PostLikeService;
import com.campusconnect.service.PostService;
import com.campusconnect.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostLikeServiceImpl extends ServiceImpl<PostLikeMapper, PostLike> implements PostLikeService {

    private final PostService postService;

    // 使用 @Lazy 避免循环依赖
    private UserService userService;
    private NotificationService notificationService;

    @org.springframework.beans.factory.annotation.Autowired
    public void setUserService(@Lazy UserService userService) {
        this.userService = userService;
    }

    @org.springframework.beans.factory.annotation.Autowired
    public void setNotificationService(@Lazy NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleLike(Long userId, Long postId) {
        // Check if already liked
        LambdaQueryWrapper<PostLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostLike::getUserId, userId)
                .eq(PostLike::getPostId, postId);

        long count = this.count(wrapper);

        if (count > 0) {
            // Already liked, so unlike
            this.remove(wrapper);
            // Decrement post like count
            postService.lambdaUpdate()
                    .eq(Post::getId, postId)
                    .setSql("like_count = GREATEST(like_count - 1, 0)")
                    .update();
            return false; // Result is "unliked"
        } else {
            // Not liked, so like
            PostLike like = new PostLike();
            like.setUserId(userId);
            like.setPostId(postId);
            this.save(like);
            // Increment post like count
            postService.lambdaUpdate()
                    .eq(Post::getId, postId)
                    .setSql("like_count = like_count + 1")
                    .update();

            // 发送点赞通知
            sendLikeNotification(userId, postId);

            return true; // Result is "liked"
        }
    }

    @Override
    public boolean isLiked(Long userId, Long postId) {
        if (userId == null)
            return false;
        return this.exists(new LambdaQueryWrapper<PostLike>()
                .eq(PostLike::getUserId, userId)
                .eq(PostLike::getPostId, postId));
    }

    @Override
    public java.util.List<Long> getLikedPostIds(Long userId, java.util.List<Long> postIds) {
        if (userId == null || postIds == null || postIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return this.list(new LambdaQueryWrapper<PostLike>()
                .eq(PostLike::getUserId, userId)
                .in(PostLike::getPostId, postIds))
                .stream()
                .map(PostLike::getPostId)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likePost(Long userId, Long postId) {
        if (!isLiked(userId, postId)) {
            PostLike like = new PostLike();
            like.setUserId(userId);
            like.setPostId(postId);
            this.save(like);
            postService.lambdaUpdate()
                    .eq(Post::getId, postId)
                    .setSql("like_count = like_count + 1")
                    .update();

            // 发送点赞通知
            sendLikeNotification(userId, postId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlikePost(Long userId, Long postId) {
        LambdaQueryWrapper<PostLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostLike::getUserId, userId).eq(PostLike::getPostId, postId);
        if (this.remove(wrapper)) {
            postService.lambdaUpdate()
                    .eq(Post::getId, postId)
                    .setSql("like_count = GREATEST(like_count - 1, 0)")
                    .update();
        }
    }

    /**
     * 发送点赞通知
     */
    private void sendLikeNotification(Long likerId, Long postId) {
        if (notificationService == null || userService == null)
            return;

        Post post = postService.getById(postId);
        if (post == null)
            return;

        // 不给自己发通知
        if (post.getUserId().equals(likerId))
            return;

        User liker = userService.getById(likerId);
        if (liker == null)
            return;

        String likerName = liker.getNickname() != null ? liker.getNickname() : liker.getUsername();
        String contentPreview = post.getContent();
        if (contentPreview != null && contentPreview.length() > 20) {
            contentPreview = contentPreview.substring(0, 20) + "...";
        }

        notificationService.createNotification(
                post.getUserId(), // 接收者：帖子作者
                likerId, // 发送者：点赞的人
                "LIKE", // 类型
                "收到点赞", // 标题
                likerName + " 赞了你的动态「" + contentPreview + "」", // 内容
                postId, // 目标ID：帖子ID
                "POST" // 目标类型
        );
    }
}
