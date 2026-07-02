package com.campusconnect.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("group_buy")
public class GroupBuy {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 拼团标题
     */
    private String title;

    /**
     * 拼团说明
     */
    private String description;

    /**
     * 分类：饮品拼单 / 外卖拼单 / 学习资料 / 生活服务
     */
    private String category;

    /**
     * 封面图
     */
    private String coverImage;

    /**
     * 发起人ID
     */
    private Long initiatorId;

    /**
     * 目标人数
     */
    private Integer targetCount;

    /**
     * 当前人数
     */
    private Integer currentCount;

    /**
     * 地点
     */
    private String location;

    /**
     * 联系方式
     */
    private String contactInfo;

    /**
     * 截止时间
     */
    private LocalDateTime deadline;

    /**
     * 状态：GROUPING 拼团中 / SUCCESS 已成团 / CANCELLED 已取消 / EXPIRED 已过期
     */
    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}