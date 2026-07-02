package com.campusconnect.dto;

import com.campusconnect.entity.Post;
import com.campusconnect.entity.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 帖子视图对象 - 用于前端展示
 */
@Data
public class PostVO {
    private Long id;
    private Long userId;
    private String content;
    private List<String> images; // 数组格式
    private List<String> tags; // 数组格式
    private String status;
    private Boolean isPinned;
    private Boolean isAnonymous;
    private Integer likeCount;
    private Integer commentCount;
    private Integer shareCount;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 用户信息
    private AuthorVO author;

    // 交互状态
    private Boolean isLiked;
    private Boolean isCollected;

    @Data
    public static class AuthorVO {
        private Long id;
        private String username;
        private String nickname;
        private String avatar;
        private String role;
        private String verifyType;
    }

    /**
     * 从 Post 实体转换为 PostVO
     */
    public static PostVO fromPost(Post post, ObjectMapper objectMapper) {
        PostVO vo = new PostVO();
        vo.setId(post.getId());
        vo.setUserId(post.getUserId());
        vo.setContent(post.getContent());
        vo.setStatus(post.getStatus());
        vo.setIsPinned(post.getIsPinned());
        vo.setIsAnonymous(post.getIsAnonymous());
        vo.setLikeCount(post.getLikeCount());
        vo.setCommentCount(post.getCommentCount());
        vo.setShareCount(post.getShareCount());
        vo.setViewCount(post.getViewCount());
        vo.setCreatedAt(post.getCreatedAt());
        vo.setUpdatedAt(post.getUpdatedAt());
        vo.setIsLiked(post.getIsLiked());
        vo.setIsCollected(post.getIsCollected());

        // 解析 JSON 字符串为数组
        try {
            if (post.getImages() != null && !post.getImages().isEmpty()) {
                vo.setImages(objectMapper.readValue(post.getImages(), new TypeReference<List<String>>() {
                }));
            } else {
                vo.setImages(new ArrayList<>());
            }
        } catch (Exception e) {
            vo.setImages(new ArrayList<>());
        }

        try {
            if (post.getTags() != null && !post.getTags().isEmpty()) {
                vo.setTags(objectMapper.readValue(post.getTags(), new TypeReference<List<String>>() {
                }));
            } else {
                vo.setTags(new ArrayList<>());
            }
        } catch (Exception e) {
            vo.setTags(new ArrayList<>());
        }

        // 转换作者信息
        if (post.getAuthor() != null) {
            AuthorVO authorVO = new AuthorVO();
            User author = post.getAuthor();
            authorVO.setId(author.getId());
            authorVO.setUsername(author.getUsername());
            authorVO.setNickname(author.getNickname());
            authorVO.setAvatar(author.getAvatar());
            authorVO.setRole(author.getRole());

            // 填充认证类型
            String vType = author.getVerifyType();
            if (vType == null || vType.isEmpty()) {
                if ("teacher".equalsIgnoreCase(author.getRole()))
                    vType = "TEACHER";
                else if ("department".equalsIgnoreCase(author.getRole()))
                    vType = "ORG";
            }
            authorVO.setVerifyType(vType);

            vo.setAuthor(authorVO);
        }

        return vo;
    }
}
