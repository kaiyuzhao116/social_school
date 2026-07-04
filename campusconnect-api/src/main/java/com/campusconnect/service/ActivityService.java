package com.campusconnect.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusconnect.entity.Activity;
import com.campusconnect.entity.ActivityRegistration;
import com.campusconnect.mapper.ActivityMapper;
import com.campusconnect.mapper.ActivityRegistrationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityService extends ServiceImpl<ActivityMapper, Activity> {

    private final ActivityRegistrationMapper activityRegistrationMapper;

    public List<Activity> getAllActivities() {
        return lambdaQuery()
                .orderByDesc(Activity::getCreatedAt)
                .list();
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
        lambdaUpdate()
                .eq(Activity::getId, id)
                .set(Activity::getStatus, status)
                .update();
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean registerUser(Long activityId, Long userId) {
        Long count = activityRegistrationMapper.selectCount(
                new LambdaQueryWrapper<ActivityRegistration>()
                        .eq(ActivityRegistration::getActivityId, activityId)
                        .eq(ActivityRegistration::getUserId, userId)
        );

        if (count != null && count > 0) {
            return false;
        }

        ActivityRegistration registration = new ActivityRegistration();
        registration.setActivityId(activityId);
        registration.setUserId(userId);
        registration.setCreatedAt(LocalDateTime.now());

        activityRegistrationMapper.insert(registration);

        int updated = baseMapper.increaseParticipantCount(activityId);

        if (updated == 0) {
            throw new RuntimeException("活动名额已满或活动不在报名阶段");
        }

        return true;
    }

    @Transactional
    public boolean unregisterUser(Long activityId, Long userId) {
        int deleted = activityRegistrationMapper.delete(
                new LambdaQueryWrapper<ActivityRegistration>()
                        .eq(ActivityRegistration::getActivityId, activityId)
                        .eq(ActivityRegistration::getUserId, userId)
        );

        if (deleted <= 0) {
            return false;
        }

        baseMapper.decreaseParticipantCount(activityId);

        return true;
    }

    public IPage<Activity> getUserRegisteredActivities(Long userId, int page, int size) {
        Page<ActivityRegistration> regPage = new Page<>(page, size);

        IPage<ActivityRegistration> registrations = activityRegistrationMapper.selectPage(
                regPage,
                new LambdaQueryWrapper<ActivityRegistration>()
                        .eq(ActivityRegistration::getUserId, userId)
                        .orderByDesc(ActivityRegistration::getCreatedAt)
        );

        List<Long> activityIds = registrations.getRecords()
                .stream()
                .map(ActivityRegistration::getActivityId)
                .toList();

        Page<Activity> result = new Page<>(page, size);
        result.setTotal(registrations.getTotal());

        if (activityIds.isEmpty()) {
            result.setRecords(List.of());
            return result;
        }

        List<Activity> activities = lambdaQuery()
                .in(Activity::getId, activityIds)
                .list();

        result.setRecords(activities);

        return result;
    }
    public void incrementParticipant(Long id) {
        lambdaUpdate()
                .eq(Activity::getId, id)
                .setSql("participant_count = participant_count + 1")
                .update();
    }

    public void decrementParticipant(Long id) {
        lambdaUpdate()
                .eq(Activity::getId, id)
                .setSql("participant_count = GREATEST(participant_count - 1, 0)")
                .update();
    }
}