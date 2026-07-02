package com.campusconnect.mq.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBuyEventMessage {

    /**
     * 拼团ID
     */
    private Long groupBuyId;

    /**
     * 拼团标题
     */
    private String title;

    /**
     * 发起人ID
     */
    private Long initiatorId;

    /**
     * 事件类型：GROUP_BUY_SUCCESS / GROUP_BUY_CANCELLED / GROUP_BUY_EXPIRED
     */
    private String eventType;

    /**
     * 当前状态：SUCCESS / CANCELLED / EXPIRED
     */
    private String status;

    /**
     * 当前人数
     */
    private Integer currentCount;

    /**
     * 目标人数
     */
    private Integer targetCount;

    /**
     * 事件发生时间
     */
    private LocalDateTime eventTime;
}