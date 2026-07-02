package com.campusconnect.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("verification")
public class Verification {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    // 认证类型: STUDENT-学生, TEACHER-教师, ORG-组织/部门
    private String identityType;
    
    private String realName;
    private String idNumber;      // 学号/工号/组织编号
    private String department;    // 院系/部门
    private String idCardImage;   // 证件照片URL
    
    // 状态: PENDING-待审核, APPROVED-已通过, REJECTED-已拒绝
    private String status;
    
    private String rejectReason;  // 拒绝原因
    private Long reviewerId;      // 审核人ID
    private LocalDateTime reviewedAt;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    // 非数据库字段
    @TableField(exist = false)
    private User user;
}
