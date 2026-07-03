package com.campusconnect.chat.dto;

import lombok.Data;

@Data
public class CreatePrivateConversationDTO {

    /**
     * 对方用户ID
     */
    private Long targetUserId;
}