package com.campusconnect.controller;

import com.campusconnect.common.Result;
import com.campusconnect.entity.GroupBuy;
import com.campusconnect.security.UserPrincipal;
import com.campusconnect.service.GroupBuyMemberService;
import com.campusconnect.service.GroupBuyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.campusconnect.entity.GroupBuyMember;
import com.campusconnect.service.GroupBuyMemberService;
import com.campusconnect.dto.GroupBuyOverviewDTO;
import com.campusconnect.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.util.List;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/group-buys")
@RequiredArgsConstructor
public class GroupBuyController {
    private final GroupBuyMemberService groupBuyMemberService;
    private final GroupBuyService groupBuyService;
    /**
     * 当前用户发起的拼团ID列表
     */
    @GetMapping("/my-created")
    public Result<?> getMyCreatedGroupBuys(@AuthenticationPrincipal UserPrincipal principal) {
        Long userId = getCurrentUserId(principal);

        List<Long> ids = groupBuyService.lambdaQuery()
                .eq(GroupBuy::getInitiatorId, userId)
                .list()
                .stream()
                .map(GroupBuy::getId)
                .collect(Collectors.toList());

        return Result.success(ids);
    }
    /**
     * 前台首页拼团列表
     */
    @GetMapping
    public Result<?> getGroupBuys() {
        return Result.success(groupBuyService.getActiveGroupBuys());
    }

    /**
     * 发起拼团
     */
    @PostMapping
    public Result<?> createGroupBuy(
            @RequestBody GroupBuy groupBuy,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            Long userId = getCurrentUserId(principal);
            return Result.success(groupBuyService.createGroupBuy(groupBuy, userId));
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 参加拼团
     */
    @PostMapping("/{id}/join")
    public Result<?> joinGroupBuy(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            Long userId = getCurrentUserId(principal);
            groupBuyService.joinGroupBuy(id, userId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 退出拼团
     */
    @PostMapping("/{id}/quit")
    public Result<?> quitGroupBuy(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            Long userId = getCurrentUserId(principal);
            groupBuyService.quitGroupBuy(id, userId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 取消拼团
     */
    @PostMapping("/{id}/cancel")
    public Result<?> cancelGroupBuy(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            Long userId = getCurrentUserId(principal);
            groupBuyService.cancelGroupBuy(id, userId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 本地测试时如果没有登录用户，默认用 1 号用户
     */
    private Long getCurrentUserId(UserPrincipal principal) {
        if (principal != null) {
            return principal.getId();
        }
        return 1L;
    }
    /**
     * 当前用户已参加的拼团ID列表
     */
    @GetMapping("/my-joined")
    public Result<?> getMyJoinedGroupBuys(@AuthenticationPrincipal UserPrincipal principal) {
        Long userId = getCurrentUserId(principal);

        List<Long> ids = groupBuyMemberService.lambdaQuery()
                .eq(GroupBuyMember::getUserId, userId)
                .list()
                .stream()
                .map(GroupBuyMember::getGroupBuyId)
                .collect(Collectors.toList());

        return Result.success(ids);
    }
    /**
     * 学生拼团首页聚合数据
     * 一次性返回：
     * 1. 拼团列表
     * 2. 当前用户已参加的拼团ID
     * 3. 当前用户发起的拼团ID
     * 4. 拼团统计数据
     *
     * 后端内部使用 CompletableFuture + 自定义线程池并行查询
     */
//    @GetMapping("/overview")
//    public Result<GroupBuyOverviewDTO> getOverview(@AuthenticationPrincipal UserPrincipal principal) {
//        return Result.success(groupBuyService.getOverview(principal.getId()));
//    }
    @GetMapping("/overview")
    public Result<GroupBuyOverviewDTO> getOverview(@AuthenticationPrincipal UserPrincipal principal) {
        Long userId = getCurrentUserId(principal);
        return Result.success(groupBuyService.getOverview(userId));
    }
//    @GetMapping("/overview")
//    public Result<GroupBuyOverviewDTO> getOverview(@AuthenticationPrincipal UserPrincipal principal) {
//        // 浏览器直接访问后端接口时，没有 token，principal 可能为空
//        // 这里先默认用 2 号用户测试，正式项目里还是走登录用户
//        Long userId = principal == null ? 2L : principal.getId();
//
//        return Result.success(groupBuyService.getOverview(userId));
//    }
}