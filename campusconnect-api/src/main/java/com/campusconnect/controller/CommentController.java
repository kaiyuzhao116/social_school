package com.campusconnect.controller;

import com.campusconnect.common.Result;
import com.campusconnect.entity.Comment;
import com.campusconnect.entity.Post;
import com.campusconnect.entity.User;
import com.campusconnect.security.UserPrincipal;
import com.campusconnect.service.CommentService;
import com.campusconnect.service.NotificationService;
import com.campusconnect.service.PostService;
import com.campusconnect.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    private final PostService postService;
    private final NotificationService notificationService;
    private final UserService userService;

    @GetMapping("/posts/{postId}/comments")
    public Result<?> getComments(@PathVariable Long postId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(commentService.getCommentsByPostId(postId, page, size));
    }

    @PostMapping("/posts/{postId}/comments")
    public Result<?> addComment(@PathVariable Long postId,
            @RequestBody Comment comment,
            @AuthenticationPrincipal UserPrincipal principal) {
        comment.setPostId(postId);
        comment.setUserId(principal.getId());
        comment.setLikeCount(0);
        commentService.save(comment);

        // 更新帖子评论数
        postService.lambdaUpdate()
                .eq(Post::getId, postId)
                .setSql("comment_count = comment_count + 1")
                .update();

        // 发送评论通知给帖子作者
        sendCommentNotification(principal.getId(), postId, comment.getContent());

        return Result.success(comment);
    }

    @PostMapping("/posts/{postId}/comments/{commentId}/reply")
    public Result<?> replyComment(@PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody Comment comment,
            @AuthenticationPrincipal UserPrincipal principal) {
        Comment parent = commentService.getById(commentId);
        if (parent == null) {
            return Result.notFound("评论不存在");
        }

        comment.setPostId(postId);
        comment.setUserId(principal.getId());
        comment.setParentId(commentId);
        comment.setReplyToUserId(parent.getUserId());
        comment.setLikeCount(0);
        commentService.save(comment);

        postService.lambdaUpdate()
                .eq(Post::getId, postId)
                .setSql("comment_count = comment_count + 1")
                .update();

        // 发送回复通知给被回复的人
        sendReplyNotification(principal.getId(), parent.getUserId(), postId, comment.getContent());

        return Result.success(comment);
    }

    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    public Result<?> deleteComment(@PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserPrincipal principal) {
        Comment comment = commentService.getById(commentId);
        if (comment == null) {
            return Result.notFound("评论不存在");
        }
        if (!comment.getUserId().equals(principal.getId()) && !"ADMIN".equals(principal.getRole())) {
            return Result.forbidden("无权删除此评论");
        }

        commentService.removeById(commentId);
        postService.lambdaUpdate()
                .eq(Post::getId, postId)
                .setSql("comment_count = GREATEST(comment_count - 1, 0)")
                .update();

        return Result.success();
    }

    @PostMapping("/posts/{postId}/comments/{commentId}/like")
    public Result<?> likeComment(@PathVariable Long postId, @PathVariable Long commentId) {
        commentService.lambdaUpdate()
                .eq(Comment::getId, commentId)
                .setSql("like_count = like_count + 1")
                .update();
        return Result.success();
    }

    @DeleteMapping("/posts/{postId}/comments/{commentId}/like")
    public Result<?> unlikeComment(@PathVariable Long postId, @PathVariable Long commentId) {
        commentService.lambdaUpdate()
                .eq(Comment::getId, commentId)
                .setSql("like_count = GREATEST(like_count - 1, 0)")
                .update();
        return Result.success();
    }

    /**
     * 发送评论通知给帖子作者
     */
    private void sendCommentNotification(Long commenterId, Long postId, String commentContent) {
        Post post = postService.getById(postId);
        if (post == null)
            return;

        // 不给自己发通知
        if (post.getUserId().equals(commenterId))
            return;

        User commenter = userService.getById(commenterId);
        if (commenter == null)
            return;

        String commenterName = commenter.getNickname() != null ? commenter.getNickname() : commenter.getUsername();
        String contentPreview = commentContent;
        if (contentPreview != null && contentPreview.length() > 30) {
            contentPreview = contentPreview.substring(0, 30) + "...";
        }

        notificationService.createNotification(
                post.getUserId(), // 接收者：帖子作者
                commenterId, // 发送者：评论的人
                "COMMENT", // 类型
                "收到评论", // 标题
                commenterName + " 评论了你的动态：" + contentPreview, // 内容
                postId, // 目标ID：帖子ID
                "POST" // 目标类型
        );
    }

    /**
     * 发送回复通知给被回复的人
     */
    private void sendReplyNotification(Long replierId, Long replyToUserId, Long postId, String replyContent) {
        // 不给自己发通知
        if (replyToUserId.equals(replierId))
            return;

        User replier = userService.getById(replierId);
        if (replier == null)
            return;

        String replierName = replier.getNickname() != null ? replier.getNickname() : replier.getUsername();
        String contentPreview = replyContent;
        if (contentPreview != null && contentPreview.length() > 30) {
            contentPreview = contentPreview.substring(0, 30) + "...";
        }

        notificationService.createNotification(
                replyToUserId, // 接收者：被回复的人
                replierId, // 发送者：回复的人
                "COMMENT", // 类型
                "收到回复", // 标题
                replierName + " 回复了你：" + contentPreview, // 内容
                postId, // 目标ID：帖子ID
                "POST" // 目标类型
        );
    }
}
