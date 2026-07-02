package com.campusconnect.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("lost_found")
@JsonIgnoreProperties(ignoreUnknown = true)
public class LostFound {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    // 类型: LOST-丢失, FOUND-拾到
    private String type;

    private String title;
    private String description;
    private String images; // 图片URL，JSON格式
    private String location; // 丢失/拾到地点
    private String contactInfo; // 联系方式

    // 状态: OPEN-进行中, CLOSED-已解决
    // 状态: OPEN-进行中, CLOSED-已解决
    private String status;

    // 优先级: NORMAL-普通, PINNED-置顶
    private String priority;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;

    // 非数据库字段
    @TableField(exist = false)
    private User user;
}
