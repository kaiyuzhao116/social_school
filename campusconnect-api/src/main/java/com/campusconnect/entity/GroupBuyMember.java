package com.campusconnect.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("group_buy_member")
public class GroupBuyMember {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 拼团ID
     */
    private Long groupBuyId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 角色：OWNER 发起人 / MEMBER 成员
     */
    private String role;

    /**
     * 加入时间
     */
    private LocalDateTime joinedAt;
}