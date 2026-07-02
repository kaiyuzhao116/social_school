package com.campusconnect.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusconnect.entity.GroupBuy;
import com.campusconnect.entity.GroupBuyMember;
import com.campusconnect.mapper.GroupBuyMapper;
import com.campusconnect.mq.event.GroupBuyEventMessage;
import com.campusconnect.mq.producer.GroupBuyEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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

    private final GroupBuyExpireProducer groupBuyExpireProducer;
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

        save(groupBuy);
         // 拼团创建成功后，注册一条 RabbitMQ 延迟过期检查消息
        // 使用 afterCommit，避免数据库事务回滚但消息已经发出去
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    groupBuyExpireProducer.sendExpireCheckMessage(
                            groupBuy.getId(),
                            groupBuy.getTitle(),
                            groupBuy.getDeadline()
                    );
                }
            });
        } else {
            groupBuyExpireProducer.sendExpireCheckMessage(
                    groupBuy.getId(),
                    groupBuy.getTitle(),
                    groupBuy.getDeadline()
            );
        }
        GroupBuyMember owner = new GroupBuyMember();
        owner.setGroupBuyId(groupBuy.getId());
        owner.setUserId(userId);
        owner.setRole("OWNER");
        owner.setJoinedAt(LocalDateTime.now());
        groupBuyMemberService.save(owner);

        return groupBuy;
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

            groupBuyEventProducer.sendSuccessEvent(
                    buildGroupBuyEventMessage(groupBuy, "GROUP_BUY_SUCCESS", "SUCCESS")
            );
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
}