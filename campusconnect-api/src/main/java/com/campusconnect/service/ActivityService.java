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
import org.redisson.api.RedissonClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.redisson.api.RLock;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityService extends ServiceImpl<ActivityMapper, Activity> {
    //操作报名表
    private final ActivityRegistrationMapper activityRegistrationMapper;
    //操作 Redis 库存和报名用户集合
    private final StringRedisTemplate stringRedisTemplate;
    //拿分布式锁
    private final RedissonClient redissonClient;

    private static final String REGISTER_LUA = """
    local stockKey = KEYS[1]
    local userSetKey = KEYS[2]
    local userId = ARGV[1]

    local stock = tonumber(redis.call('GET', stockKey) or '0')

    if stock <= 0 then
        return 1
    end

    if redis.call('SISMEMBER', userSetKey, userId) == 1 then
        return 2
    end

    redis.call('DECR', stockKey)
    redis.call('SADD', userSetKey, userId)

    return 0
    """;

    private String stockKey(Long activityId) {
        return "activity:stock:" + activityId;
    }

    private String userSetKey(Long activityId) {
        return "activity:users:" + activityId;
    }

    private String userLockKey(Long activityId, Long userId) {
        return "lock:activity:" + activityId + ":user:" + userId;
    }

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
    private void initRedisStockIfAbsent(Long activityId) {
        String stockKey = stockKey(activityId);
        String userSetKey = userSetKey(activityId);

        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(stockKey))) {
            return;
        }

        RLock initLock = redissonClient.getLock("lock:activity:init:" + activityId);

        try {
            initLock.lock(5, TimeUnit.SECONDS);

            if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(stockKey))) {
                return;
            }

            Activity activity = baseMapper.selectById(activityId);
            if (activity == null) {
                throw new RuntimeException("活动不存在");
            }

            Integer max = activity.getMaxParticipants();
            Integer current = activity.getParticipantCount();

            int stock = Math.max((max == null ? 0 : max) - (current == null ? 0 : current), 0);

            stringRedisTemplate.opsForValue().set(stockKey, String.valueOf(stock));

            List<ActivityRegistration> registrations = activityRegistrationMapper.selectList(
                    new LambdaQueryWrapper<ActivityRegistration>()
                            .eq(ActivityRegistration::getActivityId, activityId)
            );

            if (registrations != null && !registrations.isEmpty()) {
                String[] userIds = registrations.stream()
                        .map(ActivityRegistration::getUserId)
                        .map(String::valueOf)
                        .toArray(String[]::new);

                stringRedisTemplate.opsForSet().add(userSetKey, userIds);
            }

        } finally {
            if (initLock.isHeldByCurrentThread()) {
                initLock.unlock();
            }
        }
    }
    private void rollbackRedisRegister(Long activityId, Long userId) {
        stringRedisTemplate.opsForValue().increment(stockKey(activityId));
        stringRedisTemplate.opsForSet().remove(userSetKey(activityId), String.valueOf(userId));
    }
    @Transactional(rollbackFor = Exception.class)
    public boolean registerUser(Long activityId, Long userId) {
        // 1. 初始化 Redis 库存和已报名用户集合
        initRedisStockIfAbsent(activityId);

        String stockKey = stockKey(activityId);
        String userSetKey = userSetKey(activityId);

        // 2. 执行 Lua 脚本，原子判断库存和重复报名
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(REGISTER_LUA);
        script.setResultType(Long.class);

        Long luaResult = stringRedisTemplate.execute(
                script,
                List.of(stockKey, userSetKey),
                String.valueOf(userId)
        );

        if (luaResult == null) {
            return false;
        }

        // 1 = 库存不足
        if (luaResult == 1) {
            return false;
        }

        // 2 = 用户已报名
        if (luaResult == 2) {
            return false;
        }

        // 3. Redisson 锁：控制同一用户同一活动并发操作
        RLock lock = redissonClient.getLock(userLockKey(activityId, userId));
        boolean locked = false;

        try {
            locked = lock.tryLock(3, 10, TimeUnit.SECONDS);

            if (!locked) {
                rollbackRedisRegister(activityId, userId);
                return false;
            }

            // 4. 插入 MySQL 报名记录
            ActivityRegistration registration = new ActivityRegistration();
            registration.setActivityId(activityId);
            registration.setUserId(userId);
            registration.setCreatedAt(LocalDateTime.now());

            activityRegistrationMapper.insert(registration);

            // 5. MySQL 条件更新人数，最终兜底防超卖
            int updated = baseMapper.increaseParticipantCount(activityId);

            if (updated == 0) {
                rollbackRedisRegister(activityId, userId);
                throw new RuntimeException("活动名额已满或活动不在报名阶段");
            }

            return true;

        } catch (DuplicateKeyException e) {
            // MySQL 唯一索引兜底：activity_id + user_id 重复
            rollbackRedisRegister(activityId, userId);
            return false;
        } catch (Exception e) {
            rollbackRedisRegister(activityId, userId);
            throw new RuntimeException("报名失败", e);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean unregisterUser(Long activityId, Long userId) {
        initRedisStockIfAbsent(activityId);

        RLock lock = redissonClient.getLock(userLockKey(activityId, userId));
        boolean locked = false;

        try {
            locked = lock.tryLock(3, 10, TimeUnit.SECONDS);

            if (!locked) {
                return false;
            }

            int deleted = activityRegistrationMapper.delete(
                    new LambdaQueryWrapper<ActivityRegistration>()
                            .eq(ActivityRegistration::getActivityId, activityId)
                            .eq(ActivityRegistration::getUserId, userId)
            );

            if (deleted <= 0) {
                return false;
            }

            baseMapper.decreaseParticipantCount(activityId);

            // MySQL 取消成功后，同步恢复 Redis 库存和报名集合
            stringRedisTemplate.opsForValue().increment(stockKey(activityId));
            stringRedisTemplate.opsForSet().remove(userSetKey(activityId), String.valueOf(userId));

            return true;

        } catch (Exception e) {
            throw new RuntimeException("取消报名失败", e);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
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