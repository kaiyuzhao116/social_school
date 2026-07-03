package com.campusconnect.chat.vo;

import lombok.Data;

@Data
public class PrivateChatUserVO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户名
     */
    private String username;
}