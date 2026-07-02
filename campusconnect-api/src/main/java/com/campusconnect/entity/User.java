package com.campusconnect.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;
    private String password;
    private String nickname;
    private String avatar;
    private String coverImage;
    private String email;
    private String phone;
    private String bio;
    private String gender;
    
    // 校园信息
    private String college;      // 学院
    private String major;        // 专业
    private String className;    // 班级
    private String dormitory;    // 宿舍
    private Integer age;         // 年龄
    private String hobbies;      // 个人标签，JSON数组格式
    private String privacy;      // 隐私设置，JSON对象格式
    
    // 角色: USER-普通用户, MODERATOR-协管员, ADMIN-管理员
    private String role;
    
    // 状态: NORMAL-正常, BANNED-封禁
    private String status;
    
    // 认证状态: NONE-未认证, PENDING-审核中, VERIFIED-已认证
    private String verifyStatus;
    private String verifyType;  // STUDENT-学生, TEACHER-教师, ORG-组织
    
    private Integer followingCount;
    private Integer followerCount;
    private Integer postCount;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    @TableLogic
    private Integer deleted;
}
