package com.campusconnect.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("activity_registration")
public class ActivityRegistration {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long activityId;

    private Long userId;

    private LocalDateTime createdAt;
}