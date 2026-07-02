package com.campusconnect.dto;

import com.campusconnect.entity.GroupBuy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 学生拼团首页聚合数据
 *
 * 用于一次性返回：
 * 1. 拼团列表
 * 2. 当前用户已参加的拼团ID
 * 3. 当前用户发起的拼团ID
 * 4. 拼团统计数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBuyOverviewDTO {

    /**
     * 首页展示的拼团列表
     */
    private List<GroupBuy> groupBuys;

    /**
     * 当前用户已经参加的拼团ID
     */
    private List<Long> joinedIds;

    /**
     * 当前用户自己发起的拼团ID
     */
    private List<Long> createdIds;

    /**
     * 拼团统计数据
     */
    private Stats stats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Stats {

        /**
         * 拼团总数
         */
        private Long total;

        /**
         * 正在拼团数量
         */
        private Long grouping;

        /**
         * 已成团数量
         */
        private Long success;

        /**
         * 已取消数量
         */
        private Long cancelled;
    }
}