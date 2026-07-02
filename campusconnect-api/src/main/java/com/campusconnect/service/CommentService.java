package com.campusconnect.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusconnect.entity.Comment;
import com.campusconnect.entity.User;
import com.campusconnect.mapper.CommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService extends ServiceImpl<CommentMapper, Comment> {
    private final UserService userService;

    public IPage<Comment> getCommentsByPostId(Long postId, int page, int size) {
        Page<Comment> p = new Page<>(page, size);
        IPage<Comment> result = lambdaQuery()
                .eq(Comment::getPostId, postId)
                .isNull(Comment::getParentId)  // 只获取一级评论
                .orderByDesc(Comment::getCreatedAt)
                .page(p);
        
        // 填充作者信息和回复
        for (Comment comment : result.getRecords()) {
            fillAuthor(comment);
            // 获取回复
            List<Comment> replies = lambdaQuery()
                    .eq(Comment::getParentId, comment.getId())
                    .orderByAsc(Comment::getCreatedAt)
                    .list();
            replies.forEach(this::fillAuthor);
            comment.setReplies(replies);
        }
        
        return result;
    }

    private void fillAuthor(Comment comment) {
        User user = userService.getById(comment.getUserId());
        if (user != null) {
            user.setPassword(null);
            comment.setAuthor(user);
        }
        if (comment.getReplyToUserId() != null) {
            User replyTo = userService.getById(comment.getReplyToUserId());
            if (replyTo != null) {
                replyTo.setPassword(null);
                comment.setReplyToUser(replyTo);
            }
        }
    }
}
