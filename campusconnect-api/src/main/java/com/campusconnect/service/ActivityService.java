package com.campusconnect.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusconnect.entity.Activity;
import com.campusconnect.mapper.ActivityMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ActivityService extends ServiceImpl<ActivityMapper, Activity> {
    // 简单的报名记录存储（实际应该用数据库表）
    private final Map<String, Boolean> registrations = new ConcurrentHashMap<>();

    public List<Activity> getAllActivities() {
        return lambdaQuery().orderByDesc(Activity::getCreatedAt).list();
    }

    public IPage<Activity> getActivitiesPage(int page, int size, String status) {
        Page<Activity> p = new Page<>(page, size);
        return lambdaQuery()
                .eq(status != null, Activity::getStatus, status)
                .orderByDesc(Activity::getCreatedAt)
                .page(p);
    }

    public List<Activity> getActiveActivities() {
        return lambdaQuery()
                .in(Activity::getStatus, "REGISTERING", "ONGOING")
                .orderByAsc(Activity::getStartTime)
                .list();
    }

    public void updateStatus(Long id, String status) {
        lambdaUpdate().eq(Activity::getId, id).set(Activity::getStatus, status).update();
    }

    public boolean registerUser(Long activityId, Long userId) {
        String key = activityId + "_" + userId;
        if (registrations.containsKey(key)) {
            return false;
        }
        registrations.put(key, true);
        lambdaUpdate().eq(Activity::getId, activityId)
                .setSql("participant_count = participant_count + 1")
                .update();
        return true;
    }

    public void unregisterUser(Long activityId, Long userId) {
        String key = activityId + "_" + userId;
        if (registrations.remove(key) != null) {
            lambdaUpdate().eq(Activity::getId, activityId)
                    .setSql("participant_count = GREATEST(participant_count - 1, 0)")
                    .update();
        }
    }

    public IPage<Activity> getUserRegisteredActivities(Long userId, int page, int size) {
        // 简化实现：返回所有活动
        return getActivitiesPage(page, size, null);
    }

    public void incrementParticipant(Long id) {
        lambdaUpdate().eq(Activity::getId, id)
                .setSql("participant_count = participant_count + 1")
                .update();
    }

    public void decrementParticipant(Long id) {
        lambdaUpdate().eq(Activity::getId, id)
                .setSql("participant_count = GREATEST(participant_count - 1, 0)")
                .update();
    }
}
