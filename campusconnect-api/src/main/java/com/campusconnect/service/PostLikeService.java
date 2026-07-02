package com.campusconnect.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campusconnect.entity.PostLike;

public interface PostLikeService extends IService<PostLike> {
    /**
     * 点赞/取消点赞
     * 
     * @param userId 用户ID
     * @param postId 帖子ID
     * @return true=点赞成功, false=取消点赞
     */
    boolean toggleLike(Long userId, Long postId);

    /**
     * 检查用户是否点赞过某些帖子
     * 
     * @param userId 用户ID
     * @param postId 帖子ID
     * @return
     */
    boolean isLiked(Long userId, Long postId);

    /**
     * 获取用户点赞过的帖子ID列表（批量）
     * 
     * @param userId  用户ID
     * @param postIds 帖子ID列表
     * @return 点赞过的帖子ID列表
     */
    java.util.List<Long> getLikedPostIds(Long userId, java.util.List<Long> postIds);

    /**
     * 确保点赞（幂等）
     */
    void likePost(Long userId, Long postId);

    /**
     * 确保取消点赞（幂等）
     */
    void unlikePost(Long userId, Long postId);
}
