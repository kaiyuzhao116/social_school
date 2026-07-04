package com.campusconnect.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campusconnect.entity.Activity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ActivityMapper extends BaseMapper<Activity> {

    /**
     * 报名人数 +1
     * 条件更新，防止超过最大报名人数
     */
    @Update("UPDATE activity " +
            "SET participant_count = participant_count + 1 " +
            "WHERE id = #{activityId} " +
            "AND status = 'REGISTERING' " +
            "AND participant_count < max_participants")
    int increaseParticipantCount(@Param("activityId") Long activityId);

    /**
     * 报名人数 -1
     * 取消报名时使用
     */
    @Update("UPDATE activity " +
            "SET participant_count = GREATEST(participant_count - 1, 0) " +
            "WHERE id = #{activityId} " +
            "AND participant_count > 0")
    int decreaseParticipantCount(@Param("activityId") Long activityId);
}