package com.campusconnect.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("activity")
public class Activity {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String title;
    private String description;
    private String coverImage;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String organizer;      // 主办方
    private Long organizerId;      // 发布人ID
    
    // 状态: REGISTERING-报名中, ONGOING-进行中, ENDED-已结束
    private String status;
    
    private Integer maxParticipants;  // 最大参与人数
    private Integer participantCount; // 当前参与人数
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    @TableLogic
    private Integer deleted;
}
