package com.campusconnect.mq.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 拼团过期检查消息
 *
 * 这个消息只负责“到点后提醒系统去检查”。
 * 真正是否过期，要由消费者再次查询数据库判断。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBuyExpireCheckMessage {

    /**
     * 拼团ID
     */
    private Long groupBuyId;

    /**
     * 拼团标题，方便日志展示
     */
    private String title;

    /**
     * 拼团截止时间
     */
    private LocalDateTime deadline;

    /**
     * 消息创建时间
     */
    private LocalDateTime createTime;
}