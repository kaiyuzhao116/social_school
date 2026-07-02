package com.campusconnect.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusconnect.entity.GroupBuy;
import com.campusconnect.entity.GroupBuyMember;
import com.campusconnect.mapper.GroupBuyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupBuyService extends ServiceImpl<GroupBuyMapper, GroupBuy> {

    private final GroupBuyMemberService groupBuyMemberService;

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
        GroupBuy groupBuy = getById(groupBuyId);

        if (groupBuy == null) {
            throw new RuntimeException("拼团不存在");
        }

        if (!"GROUPING".equals(groupBuy.getStatus())) {
            throw new RuntimeException("当前拼团不可加入");
        }

        if (groupBuy.getDeadline() != null && groupBuy.getDeadline().isBefore(LocalDateTime.now())) {
            lambdaUpdate()
                    .eq(GroupBuy::getId, groupBuyId)
                    .set(GroupBuy::getStatus, "EXPIRED")
                    .update();
            throw new RuntimeException("拼团已过期");
        }

        if (groupBuy.getCurrentCount() >= groupBuy.getTargetCount()) {
            throw new RuntimeException("拼团人数已满");
        }

        boolean joined = groupBuyMemberService.lambdaQuery()
                .eq(GroupBuyMember::getGroupBuyId, groupBuyId)
                .eq(GroupBuyMember::getUserId, userId)
                .exists();

        if (joined) {
            throw new RuntimeException("你已经参加过该拼团");
        }

        GroupBuyMember member = new GroupBuyMember();
        member.setGroupBuyId(groupBuyId);
        member.setUserId(userId);
        member.setRole("MEMBER");
        member.setJoinedAt(LocalDateTime.now());
        groupBuyMemberService.save(member);

        int newCount = groupBuy.getCurrentCount() + 1;
        String newStatus = newCount >= groupBuy.getTargetCount() ? "SUCCESS" : "GROUPING";

        lambdaUpdate()
                .eq(GroupBuy::getId, groupBuyId)
                .set(GroupBuy::getCurrentCount, newCount)
                .set(GroupBuy::getStatus, newStatus)
                .update();
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
    }
}