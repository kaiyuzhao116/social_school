package com.campusconnect.dto;

import lombok.Data;
import java.util.List;

/**
 * 创建帖子请求 DTO
 */
@Data
public class CreatePostRequest {
    private String content;
    private List<String> images;
    private List<String> tags;
    private Boolean isAnonymous;
}
