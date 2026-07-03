package com.campusconnect.chat.vo;

import lombok.Data;

@Data
public class ChatMessageReadUserVO {

    private Long userId;

    private String username;

    private String nickname;

    private String readTime;
}