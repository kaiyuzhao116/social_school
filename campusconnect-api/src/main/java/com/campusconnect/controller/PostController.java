package com.campusconnect.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campusconnect.common.Result;
import com.campusconnect.dto.CreatePostRequest;
import com.campusconnect.dto.PostVO;
import com.campusconnect.entity.Post;
import com.campusconnect.entity.User;
import com.campusconnect.security.UserPrincipal;
import com.campusconnect.service.PostService;
import com.campusconnect.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final com.campusconnect.service.PostLikeService postLikeService;

    @GetMapping
    public Result<?> getPosts(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal principal) {
        IPage<Post> postPage = postService.getPosts(page, size, "PUBLISHED");
        List<PostVO> voList = postPage.getRecords().stream()
                .map(p -> PostVO.fromPost(p, objectMapper))
                .collect(Collectors.toList());

        // 批量填充点赞状态
        if (principal != null) {
            populatePostLikeStatus(voList, principal.getId());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", voList);
        result.put("total", postPage.getTotal());
        result.put("pages", postPage.getPages());
        result.put("current", postPage.getCurrent());
        result.put("size", postPage.getSize());
        return Result.success(result);
    }

    @GetMapping("/hot")
    public Result<?> getHotPosts(@RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal UserPrincipal principal) {
        List<Post> posts = postService.getHotPosts(limit);
        List<PostVO> voList = posts.stream()
                .map(p -> PostVO.fromPost(p, objectMapper))
                .collect(Collectors.toList());

        if (principal != null) {
            populatePostLikeStatus(voList, principal.getId());
        }

        return Result.success(voList);
    }

    @GetMapping("/recommended")
    public Result<?> getRecommendedPosts(@RequestParam(defaultValue = "20") int limit,
            @AuthenticationPrincipal UserPrincipal principal) {
        List<Post> posts;

        if (principal != null) {
            // 已登录用户：个性化推荐
            posts = postService.getRecommendedPosts(principal.getId(), limit);
        } else {
            // 未登录用户：返回热门帖子
            posts = postService.getHotPosts(limit);
        }

        List<PostVO> voList = posts.stream()
                .map(p -> PostVO.fromPost(p, objectMapper))
                .collect(Collectors.toList());

        if (principal != null) {
            populatePostLikeStatus(voList, principal.getId());
        }

        return Result.success(voList);
    }

    @GetMapping("/search")
    public Result<?> searchPosts(@RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal principal) {
        IPage<Post> postPage = postService.searchPosts(keyword, page, size);
        List<PostVO> voList = postPage.getRecords().stream()
                .map(p -> PostVO.fromPost(p, objectMapper))
                .collect(Collectors.toList());

        if (principal != null) {
            populatePostLikeStatus(voList, principal.getId());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", voList);
        result.put("total", postPage.getTotal());
        result.put("pages", postPage.getPages());
        result.put("current", postPage.getCurrent());
        result.put("size", postPage.getSize());
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<?> getPost(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        Post post = postService.getById(id);
        if (post == null) {
            return Result.notFound("帖子不存在");
        }
        // 填充作者信息
        User author = userService.getById(post.getUserId());
        if (author != null) {
            author.setPassword(null);
            post.setAuthor(author);
        }

        PostVO vo = PostVO.fromPost(post, objectMapper);
        if (principal != null) {
            vo.setIsLiked(postLikeService.isLiked(principal.getId(), id));
        }

        return Result.success(vo);
    }

    @PostMapping
    public Result<?> createPost(@RequestBody CreatePostRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        // 如果未认证，返回错误
        if (principal == null) {
            return Result.error("请先登录");
        }

        try {
            Post post = new Post();
            post.setUserId(principal.getId());
            post.setContent(request.getContent());
            // 将数组转换为 JSON 字符串存储
            post.setImages(request.getImages() != null && !request.getImages().isEmpty()
                    ? objectMapper.writeValueAsString(request.getImages())
                    : null);
            post.setTags(request.getTags() != null && !request.getTags().isEmpty()
                    ? objectMapper.writeValueAsString(request.getTags())
                    : null);
            post.setIsAnonymous(request.getIsAnonymous() != null && request.getIsAnonymous());
            post.setStatus("PUBLISHED"); // 直接发布，不需要审核
            post.setIsPinned(false);
            post.setLikeCount(0);
            post.setCommentCount(0);
            post.setShareCount(0);
            post.setViewCount(0);
            postService.save(post);
            postService.deleteHotPostsCache();
            // 重新从数据库获取以确保获取自动填充的字段（如 createdAt）
            post = postService.getById(post.getId());

            // 填充作者信息后返回
            User author = userService.getById(principal.getId());
            if (author != null) {
                author.setPassword(null);
                post.setAuthor(author);
            }

            return Result.success(PostVO.fromPost(post, objectMapper));
        } catch (JsonProcessingException e) {
            return Result.error("数据格式错误");
        }
    }

    @PutMapping("/{id}")
    public Result<?> updatePost(@PathVariable Long id, @RequestBody CreatePostRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        Post post = postService.getById(id);
        if (post == null) {
            return Result.notFound("帖子不存在");
        }
        // 只能编辑自己的帖子
        if (!post.getUserId().equals(principal.getId())) {
            return Result.forbidden("无权编辑此帖子");
        }

        // 检查是否超过12小时
        java.time.Duration duration = java.time.Duration.between(post.getCreatedAt(), java.time.LocalDateTime.now());
        if (duration.toHours() >= 12) {
            return Result.error("发布超过12小时的帖子无法编辑");
        }

        try {
            post.setContent(request.getContent());
            post.setImages(request.getImages() != null && !request.getImages().isEmpty()
                    ? objectMapper.writeValueAsString(request.getImages())
                    : null);
            post.setTags(request.getTags() != null && !request.getTags().isEmpty()
                    ? objectMapper.writeValueAsString(request.getTags())
                    : null);
            post.setIsAnonymous(request.getIsAnonymous() != null && request.getIsAnonymous());
            postService.updateById(post);
            postService.deleteHotPostsCache();

            // 填充作者信息
            User author = userService.getById(principal.getId());
            if (author != null) {
                author.setPassword(null);
                post.setAuthor(author);
            }

            return Result.success(PostVO.fromPost(post, objectMapper));
        } catch (JsonProcessingException e) {
            return Result.error("数据格式错误");
        }
    }

    @DeleteMapping("/{id}")
    public Result<?> deletePost(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        Post post = postService.getById(id);
        if (post == null) {
            return Result.notFound("帖子不存在");
        }
        // 只能删除自己的帖子，或者管理员可以删除任何帖子
        if (!post.getUserId().equals(principal.getId()) && !"ADMIN".equals(principal.getRole())) {
            return Result.forbidden("无权删除此帖子");
        }
        postService.removeById(id);
        return Result.success();
    }

    @PostMapping("/{id}/like")
    public Result<?> likePost(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null)
            return Result.error("请先登录");
        postLikeService.likePost(principal.getId(), id);
        postService.deleteHotPostsCache();
        return Result.success();
    }

    @DeleteMapping("/{id}/like")
    public Result<?> unlikePost(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null)
            return Result.error("请先登录");
        postLikeService.unlikePost(principal.getId(), id);
        postService.deleteHotPostsCache();
        return Result.success();
    }

    @PostMapping("/{id}/share")
    public Result<?> sharePost(@PathVariable Long id) {
        postService.lambdaUpdate().eq(Post::getId, id).setSql("share_count = share_count + 1").update();
        postService.deleteHotPostsCache();
        return Result.success();
    }

    // Helper method to batch populate like status
    private void populatePostLikeStatus(List<PostVO> voList, Long userId) {
        if (voList == null || voList.isEmpty())
            return;
        List<Long> postIds = voList.stream().map(PostVO::getId).collect(Collectors.toList());
        List<Long> likedIds = postLikeService.getLikedPostIds(userId, postIds);
        voList.forEach(vo -> vo.setIsLiked(likedIds.contains(vo.getId())));
    }
}
