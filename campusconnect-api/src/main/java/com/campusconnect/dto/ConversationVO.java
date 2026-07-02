package com.campusconnect.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ConversationVO {
    private Long id;
    private String name;
    private String type;
    private String avatar;
    private Long ownerId;
    private String announcement;
    private Integer memberCount;
    private Integer unreadCount;

    // 最后一条消息
    private String lastMessage;
    private String lastSenderName;
    private LocalDateTime updatedAt;

    // 对于私聊，显示对方信息
    private String targetName;
    private String targetAvatar;
    private Long targetId;
    private String targetVerifyType;

    // 群成员列表
    private List<MemberVO> members;
}
