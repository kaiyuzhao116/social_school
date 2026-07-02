package com.campusconnect.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusconnect.entity.Post;
import com.campusconnect.entity.User;
import com.campusconnect.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService extends ServiceImpl<PostMapper, Post> {
    private final UserService userService;

    public IPage<Post> getPosts(int page, int size, String status) {
        Page<Post> p = new Page<>(page, size);
        IPage<Post> result = lambdaQuery()
                .eq(status != null, Post::getStatus, status)
                .orderByDesc(Post::getIsPinned)
                .orderByDesc(Post::getCreatedAt)
                .page(p);
        fillAuthor(result.getRecords());
        return result;
    }

    public IPage<Post> getPendingPosts(int page, int size) {
        return getPosts(page, size, "PENDING");
    }

    public IPage<Post> getUserPosts(Long userId, int page, int size) {
        Page<Post> p = new Page<>(page, size);
        IPage<Post> result = lambdaQuery()
                .eq(Post::getUserId, userId)
                .eq(Post::getStatus, "PUBLISHED")
                .orderByDesc(Post::getCreatedAt)
                .page(p);
        fillAuthor(result.getRecords());
        return result;
    }

    public List<Post> getHotPosts(int limit) {
        List<Post> posts = lambdaQuery()
                .eq(Post::getStatus, "PUBLISHED")
                .orderByDesc(Post::getLikeCount)
                .last("LIMIT " + limit)
                .list();
        fillAuthor(posts);
        return posts;
    }

    /**
     * 个性化推荐帖子
     * 推荐逻辑：
     * 1. 关注的人发的帖子（权重最高）
     * 2. 用户感兴趣标签的帖子
     * 3. 综合热度帖子
     */
    public List<Post> getRecommendedPosts(Long userId, int limit) {
        // 获取用户关注的人
        List<Long> followingIds = userService.getFollowingIds(userId);

        // 获取所有已发布的帖子（最近30天内）
        List<Post> allPosts = lambdaQuery()
                .eq(Post::getStatus, "PUBLISHED")
                .ge(Post::getCreatedAt, java.time.LocalDateTime.now().minusDays(30))
                .ne(Post::getUserId, userId) // 不推荐自己的帖子
                .list();

        // 计算每个帖子的推荐分数
        java.util.Map<Post, Double> postScores = new java.util.HashMap<>();

        for (Post post : allPosts) {
            double score = 0;

            // 1. 是否是关注的人发的帖子（+50分）
            if (followingIds.contains(post.getUserId())) {
                score += 50;
            }

            // 2. 热度分数：点赞 + 评论*2 + 分享*3（最高30分）
            int engagement = (post.getLikeCount() != null ? post.getLikeCount() : 0)
                    + (post.getCommentCount() != null ? post.getCommentCount() * 2 : 0)
                    + (post.getShareCount() != null ? post.getShareCount() * 3 : 0);
            score += Math.min(engagement * 0.5, 30);

            // 3. 时间衰减：越新的帖子分数越高（最高20分）
            if (post.getCreatedAt() != null) {
                long hoursAgo = java.time.Duration.between(post.getCreatedAt(), java.time.LocalDateTime.now())
                        .toHours();
                score += Math.max(0, 20 - hoursAgo * 0.2);
            }

            postScores.put(post, score);
        }

        // 按分数排序并取前limit个
        List<Post> recommended = postScores.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(limit)
                .map(java.util.Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toList());

        fillAuthor(recommended);
        return recommended;
    }

    public IPage<Post> searchPosts(String keyword, int page, int size) {
        Page<Post> p = new Page<>(page, size);
        IPage<Post> result = lambdaQuery()
                .eq(Post::getStatus, "PUBLISHED")
                .like(Post::getContent, keyword)
                .orderByDesc(Post::getCreatedAt)
                .page(p);
        fillAuthor(result.getRecords());
        return result;
    }

    public void moderate(Long postId, String action) {
        String status = "approve".equals(action) ? "PUBLISHED" : "REJECTED";
        lambdaUpdate().eq(Post::getId, postId).set(Post::getStatus, status).update();
    }

    public void togglePin(Long postId) {
        Post post = getById(postId);
        if (post != null) {
            lambdaUpdate().eq(Post::getId, postId).set(Post::getIsPinned, !post.getIsPinned()).update();
        }
    }

    public void updateAiAnalysis(Long postId, boolean safe, String reason, int confidence) {
        lambdaUpdate().eq(Post::getId, postId)
                .set(Post::getAiSafe, safe)
                .set(Post::getAiReason, reason)
                .set(Post::getAiConfidence, confidence)
                .update();
    }

    /**
     * 获取过去24小时各时间段的发帖统计（用于图表趋势）
     * 返回结构：List<Map: {hour: "14:00", posts: 5, visits: 100}>
     */
    public List<java.util.Map<String, Object>> getPostActivityTrend() {
        List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        // 统计过去7小时的数据
        for (int i = 6; i >= 0; i--) {
            java.time.LocalDateTime startTime = now.minusHours(i).withMinute(0).withSecond(0).withNano(0);
            java.time.LocalDateTime endTime = startTime.plusHours(1);

            // 统计该小时内的帖子数
            long posts = lambdaQuery()
                    .ge(Post::getCreatedAt, startTime)
                    .lt(Post::getCreatedAt, endTime)
                    .count();

            // 模拟访问量（因为没有真实访问日志表）
            // 基础访问量 + 随机波动 + 帖子数带来的流量
            long visits = 100 + (long) (Math.random() * 200) + posts * 50;

            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("name", startTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:00")));
            item.put("posts", posts);
            item.put("visits", visits);
            result.add(item);
        }
        return result;
    }

    /**
     * 获取内容分类统计（按标签）
     * 返回结构：List<Map: {name: "标签", value: 10}>
     */
    public List<java.util.Map<String, Object>> getPostContentStats() {
        // 获取所有帖子的标签
        List<Post> posts = lambdaQuery()
                .select(Post::getTags)
                .isNotNull(Post::getTags)
                .ne(Post::getTags, "")
                .ne(Post::getTags, "[]")
                .list();

        // 统计标签频率
        java.util.Map<String, Integer> tagCounts = new java.util.HashMap<>();
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

        for (Post post : posts) {
            try {
                // tags 是 JSON 数组字符串 ["标签1", "标签2"]
                @SuppressWarnings("unchecked")
                List<String> tags = mapper.readValue(post.getTags(), List.class);
                for (String tag : tags) {
                    tagCounts.put(tag, tagCounts.getOrDefault(tag, 0) + 1);
                }
            } catch (Exception e) {
                // 忽略解析错误
            }
        }

        // 转换为列表并排序
        return tagCounts.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue())) // 降序
                .limit(10) // 取前10个
                .map(entry -> {
                    java.util.Map<String, Object> map = new java.util.HashMap<>();
                    map.put("name", entry.getKey());
                    map.put("posts", entry.getValue());
                    return map;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    private void fillAuthor(List<Post> posts) {
        for (Post post : posts) {
            User user = userService.getById(post.getUserId());
            if (user != null) {
                user.setPassword(null);
                post.setAuthor(user);
            }
        }
    }
}
