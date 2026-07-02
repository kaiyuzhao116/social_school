package com.campusconnect.service;
import com.campusconnect.dto.GroupBuyMemberDTO;
import com.campusconnect.entity.User;
import com.campusconnect.service.UserService;

import java.util.*;
import java.util.function.Function;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusconnect.entity.GroupBuy;
import com.campusconnect.entity.GroupBuyMember;
import com.campusconnect.mapper.GroupBuyMapper;
import com.campusconnect.mq.event.GroupBuyEventMessage;
import com.campusconnect.mq.producer.GroupBuyEventProducer;
import com.campusconnect.ws.GroupBuyLivePushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import com.campusconnect.dto.GroupBuyOverviewDTO;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import com.campusconnect.mq.producer.GroupBuyExpireProducer;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
@Slf4j
@Service
@RequiredArgsConstructor
public class GroupBuyService extends ServiceImpl<GroupBuyMapper, GroupBuy> {

    private final GroupBuyMemberService groupBuyMemberService;
    private final GroupBuyEventProducer groupBuyEventProducer;
    @Qualifier("groupBuyQueryExecutor")
    private final Executor groupBuyQueryExecutor;
    private final UserService userService;
    private final GroupBuyExpireProducer groupBuyExpireProducer;

    private final GroupBuyLivePushService groupBuyLivePushService;
    /**
     * 查询首页拼团列表
     */
    public List<GroupBuy> getActiveGroupBuys() {
        return lambdaQuery()
                .in(GroupBuy::getStatus, "GROUPING", "SUCCESS")
                .orderByDesc(GroupBuy::getCreatedAt)
                .list();
    }
    /**
     * 首页学生拼团聚合查询
     * 使用 CompletableFuture + 自定义线程池并行查询：
     * 1. 拼团列表
     * 2. 当前用户已参加的拼团ID
     * 3. 当前用户发起的拼团ID
     * 4. 拼团统计数据
     */
    /**
     * 学生拼团首页聚合查询
     *
     * 使用 CompletableFuture + 自定义线程池并行查询：
     * 1. 拼团列表
     * 2. 当前用户已参加的拼团ID
     * 3. 当前用户发起的拼团ID
     * 4. 拼团统计数据
     */
    public GroupBuyOverviewDTO getOverview(Long userId) {
        log.info("【拼团聚合】开始聚合查询，当前用户ID：{}，主线程：{}",
                userId,
                Thread.currentThread().getName()
        );

        CompletableFuture<List<GroupBuy>> groupBuysFuture = CompletableFuture.supplyAsync(() -> {
            log.info("【拼团聚合】查询拼团列表，线程：{}", Thread.currentThread().getName());
            return getActiveGroupBuys();
        }, groupBuyQueryExecutor);

        CompletableFuture<List<Long>> joinedIdsFuture = CompletableFuture.supplyAsync(() -> {
            log.info("【拼团聚合】查询当前用户已参加的拼团，线程：{}", Thread.currentThread().getName());

            return groupBuyMemberService.lambdaQuery()
                    .eq(GroupBuyMember::getUserId, userId)
                    .list()
                    .stream()
                    .map(GroupBuyMember::getGroupBuyId)
                    .collect(Collectors.toList());
        }, groupBuyQueryExecutor);

        CompletableFuture<List<Long>> createdIdsFuture = CompletableFuture.supplyAsync(() -> {
            log.info("【拼团聚合】查询当前用户发起的拼团，线程：{}", Thread.currentThread().getName());

            return lambdaQuery()
                    .eq(GroupBuy::getInitiatorId, userId)
                    .list()
                    .stream()
                    .map(GroupBuy::getId)
                    .collect(Collectors.toList());
        }, groupBuyQueryExecutor);

        CompletableFuture<GroupBuyOverviewDTO.Stats> statsFuture = CompletableFuture.supplyAsync(() -> {
            log.info("【拼团聚合】查询拼团统计数据，线程：{}", Thread.currentThread().getName());

            Long total = lambdaQuery().count();
            Long grouping = lambdaQuery().eq(GroupBuy::getStatus, "GROUPING").count();
            Long success = lambdaQuery().eq(GroupBuy::getStatus, "SUCCESS").count();
            Long cancelled = lambdaQuery().eq(GroupBuy::getStatus, "CANCELLED").count();

            return GroupBuyOverviewDTO.Stats.builder()
                    .total(total)
                    .grouping(grouping)
                    .success(success)
                    .cancelled(cancelled)
                    .build();
        }, groupBuyQueryExecutor);

        CompletableFuture.allOf(
                groupBuysFuture,
                joinedIdsFuture,
                createdIdsFuture,
                statsFuture
        ).join();

        GroupBuyOverviewDTO overview = GroupBuyOverviewDTO.builder()
                .groupBuys(groupBuysFuture.join())
                .joinedIds(joinedIdsFuture.join())
                .createdIds(createdIdsFuture.join())
                .stats(statsFuture.join())
                .build();

        log.info("【拼团聚合】聚合查询完成，拼团数量：{}，已参加数量：{}，已发起数量：{}",
                overview.getGroupBuys().size(),
                overview.getJoinedIds().size(),
                overview.getCreatedIds().size()
        );

        return overview;
    }


    /**
     * 参加拼团
     */
    @Transactional
    public void joinGroupBuy(Long groupBuyId, Long userId) {
        if (groupBuyId == null || userId == null) {
            throw new RuntimeException("参数不能为空");
        }

        // 1. 先判断是否已经参加过
        boolean alreadyJoined = groupBuyMemberService.lambdaQuery()
                .eq(GroupBuyMember::getGroupBuyId, groupBuyId)
                .eq(GroupBuyMember::getUserId, userId)
                .count() > 0;

        if (alreadyJoined) {
            throw new RuntimeException("你已经参加过该拼团");
        }

        // 2. 数据库原子更新人数
        // 只有 status = GROUPING 且 current_count < target_count 时才允许 +1
        boolean updated = lambdaUpdate()
                .eq(GroupBuy::getId, groupBuyId)
                .eq(GroupBuy::getStatus, "GROUPING")
                .apply("current_count < target_count")
                .setSql("current_count = current_count + 1")
                .update();

        if (!updated) {
            throw new RuntimeException("拼团已满、已结束或不可参加");
        }

        // 3. 插入拼团成员
        // 如果同一个用户并发重复点击，这里会被唯一索引兜底拦住
        GroupBuyMember member = new GroupBuyMember();
        member.setGroupBuyId(groupBuyId);
        member.setUserId(userId);
        member.setRole("MEMBER");
        member.setJoinedAt(LocalDateTime.now());

        groupBuyMemberService.save(member);

        // 4. 重新查最新拼团数据
        GroupBuy groupBuy = getById(groupBuyId);

        if (groupBuy == null) {
            throw new RuntimeException("拼团不存在");
        }

        // 5. 如果人数已经满了，把状态从 GROUPING 改成 SUCCESS
        // 这里也用条件更新，保证只有一个线程能真正把它改成 SUCCESS
        boolean successUpdated = lambdaUpdate()
                .eq(GroupBuy::getId, groupBuyId)
                .eq(GroupBuy::getStatus, "GROUPING")
                .apply("current_count >= target_count")
                .set(GroupBuy::getStatus, "SUCCESS")
                .update();

        // 6. 只有真正完成成团状态更新的线程，才发送 MQ
        if (successUpdated) {
            groupBuy.setStatus("SUCCESS");

            // 事务提交后再发送 MQ 和 WebSocket，避免数据库回滚但消息已经推送
            runAfterCommit(() -> {
                groupBuyEventProducer.sendSuccessEvent(
                        buildGroupBuyEventMessage(groupBuy, "GROUP_BUY_SUCCESS", "SUCCESS")
                );

                groupBuyLivePushService.pushSuccess(groupBuy);
            });
        } else {
            // 没有成团，只推送“有人加入拼团”
            runAfterCommit(() -> {
                groupBuyLivePushService.pushJoined(groupBuy);
            });
        }
    }
    /**
     * 发起拼团
     */
    @Transactional
    public GroupBuy createGroupBuy(GroupBuy groupBuy, Long userId) {
        groupBuy.setInitiatorId(userId);
        groupBuy.setCurrentCount(1);
        groupBuy.setStatus("GROUPING");

        if (groupBuy.getTargetCount() == null || groupBuy.getTargetCount() < 2) {
            groupBuy.setTargetCount(2);
        }

        if (groupBuy.getCategory() == null || groupBuy.getCategory().isBlank()) {
            groupBuy.setCategory("生活服务");
        }

        if (groupBuy.getCoverImage() == null || groupBuy.getCoverImage().isBlank()) {
            groupBuy.setCoverImage("https://picsum.photos/400/240?random=" + System.currentTimeMillis());
        }

        // 1. 保存拼团主表
        save(groupBuy);

        // 2. 保存发起人为 OWNER
        GroupBuyMember owner = new GroupBuyMember();
        owner.setGroupBuyId(groupBuy.getId());
        owner.setUserId(userId);
        owner.setRole("OWNER");
        owner.setJoinedAt(LocalDateTime.now());
        groupBuyMemberService.save(owner);

        // 3. 事务提交成功后，再发送延迟过期检查消息 + WebSocket 实时卡片
        runAfterCommit(() -> {
            // RabbitMQ TTL + DLX 过期检查消息
            groupBuyExpireProducer.sendExpireCheckMessage(
                    groupBuy.getId(),
                    groupBuy.getTitle(),
                    groupBuy.getDeadline()
            );

            // WebSocket 实时推送：新拼团发布
            groupBuyLivePushService.pushCreated(groupBuy);
        });

        return groupBuy;
    }
    /**
     * 事务提交后执行任务
     *
     * 如果当前方法在事务中，就等事务提交成功后执行；
     * 如果当前没有事务，就直接执行。
     */
    private void runAfterCommit(Runnable task) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
        } else {
            task.run();
        }
    }
    /**
     * 退出拼团
     */
    @Transactional
    public void quitGroupBuy(Long groupBuyId, Long userId) {
        GroupBuy groupBuy = getById(groupBuyId);

        if (groupBuy == null) {
            throw new RuntimeException("拼团不存在");
        }

        if (userId.equals(groupBuy.getInitiatorId())) {
            throw new RuntimeException("发起人不能退出拼团，可以取消拼团");
        }

        GroupBuyMember member = groupBuyMemberService.lambdaQuery()
                .eq(GroupBuyMember::getGroupBuyId, groupBuyId)
                .eq(GroupBuyMember::getUserId, userId)
                .one();

        if (member == null) {
            throw new RuntimeException("你未参加该拼团");
        }

        groupBuyMemberService.remove(
                new LambdaQueryWrapper<GroupBuyMember>()
                        .eq(GroupBuyMember::getGroupBuyId, groupBuyId)
                        .eq(GroupBuyMember::getUserId, userId)
        );

        int newCount = Math.max(groupBuy.getCurrentCount() - 1, 1);
        String newStatus = newCount >= groupBuy.getTargetCount() ? "SUCCESS" : "GROUPING";

        lambdaUpdate()
                .eq(GroupBuy::getId, groupBuyId)
                .set(GroupBuy::getCurrentCount, newCount)
                .set(GroupBuy::getStatus, newStatus)
                .update();
    }

    /**
     * 发起人取消拼团
     */
    @Transactional
    public void cancelGroupBuy(Long groupBuyId, Long userId) {
        GroupBuy groupBuy = getById(groupBuyId);

        if (groupBuy == null) {
            throw new RuntimeException("拼团不存在");
        }

        if (!userId.equals(groupBuy.getInitiatorId())) {
            throw new RuntimeException("只有发起人可以取消拼团");
        }

        lambdaUpdate()
                .eq(GroupBuy::getId, groupBuyId)
                .set(GroupBuy::getStatus, "CANCELLED")
                .update();

        // 取消拼团后发送 MQ 事件
        groupBuy.setStatus("CANCELLED");

        groupBuyEventProducer.sendCancelledEvent(
                buildGroupBuyEventMessage(groupBuy, "GROUP_BUY_CANCELLED", "CANCELLED")
        );
    }
    /**
     * 构建拼团 MQ 事件消息
     */
    private GroupBuyEventMessage buildGroupBuyEventMessage(GroupBuy groupBuy, String eventType, String status) {
        return GroupBuyEventMessage.builder()
                .groupBuyId(groupBuy.getId())
                .title(groupBuy.getTitle())
                .initiatorId(groupBuy.getInitiatorId())
                .eventType(eventType)
                .status(status)
                .currentCount(groupBuy.getCurrentCount())
                .targetCount(groupBuy.getTargetCount())
                .eventTime(LocalDateTime.now())
                .build();
    }
    /**
     * 查询拼团成员列表
     *
     * 权限控制：
     * 1. 拼团发起人可以查看
     * 2. 已参加该拼团的成员可以查看
     * 3. 未参加的人不能查看成员和联系方式
     */
    public List<GroupBuyMemberDTO> getMembers(Long groupBuyId, Long currentUserId) {
        if (groupBuyId == null || currentUserId == null) {
            throw new RuntimeException("参数不能为空");
        }

        GroupBuy groupBuy = getById(groupBuyId);

        if (groupBuy == null) {
            throw new RuntimeException("拼团不存在");
        }

        List<GroupBuyMember> members = groupBuyMemberService.lambdaQuery()
                .eq(GroupBuyMember::getGroupBuyId, groupBuyId)
                .list();

        boolean isCreator = currentUserId.equals(groupBuy.getInitiatorId());

        boolean isMember = members.stream()
                .anyMatch(member -> currentUserId.equals(member.getUserId()));

        if (!isCreator && !isMember) {
            throw new RuntimeException("参加拼团后才可以查看成员列表");
        }

        /**
         * 收集成员 userId：
         * 1. group_buy_member 表里的成员
         * 2. 拼团发起人
         *
         * 用 Set 去重，避免发起人重复出现。
         */
        Set<Long> userIds = new LinkedHashSet<>();

        if (groupBuy.getInitiatorId() != null) {
            userIds.add(groupBuy.getInitiatorId());
        }

        for (GroupBuyMember member : members) {
            if (member.getUserId() != null) {
                userIds.add(member.getUserId());
            }
        }

        if (userIds.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, User> userMap = userService.listByIds(userIds)
                .stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<GroupBuyMemberDTO> result = new ArrayList<>();

        for (Long userId : userIds) {
            User user = userMap.get(userId);

            if (user == null) {
                continue;
            }

            String role = currentUserRole(userId, groupBuy, members);

            result.add(GroupBuyMemberDTO.builder()
                    .userId(user.getId())
                    .nickname(user.getNickname())
                    .avatar(user.getAvatar())
                    .role(role)
                    .build());
        }

        return result;
    }

    /**
     * 判断成员角色
     */
    private String currentUserRole(Long userId, GroupBuy groupBuy, List<GroupBuyMember> members) {
        if (userId.equals(groupBuy.getInitiatorId())) {
            return "OWNER";
        }

        for (GroupBuyMember member : members) {
            if (userId.equals(member.getUserId())) {
                return member.getRole();
            }
        }

        return "MEMBER";
    }
}