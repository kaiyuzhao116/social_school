package com.campusconnect.controller;

import com.campusconnect.common.Result;
import com.campusconnect.entity.*;
import com.campusconnect.security.UserPrincipal;
import com.campusconnect.service.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    private final UserService userService;
    private final PostService postService;
    private final VerificationService verificationService;
    private final ReportService reportService;
    private final AnnouncementService announcementService;
    private final ActivityService activityService;
    private final LostFoundService lostFoundService;

    // ========== 仪表盘统计 ==========
    @GetMapping("/dashboard/stats")
    public Result<?> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("userCount", userService.count());
        stats.put("postCount", postService.count());
        stats.put("pendingVerifications",
                verificationService.lambdaQuery().eq(Verification::getStatus, "PENDING").count());
        stats.put("pendingReports", reportService.lambdaQuery().eq(Report::getStatus, "PENDING").count());
        stats.put("pendingPosts", postService.lambdaQuery().eq(Post::getStatus, "PENDING").count());
        return Result.success(stats);
    }

    @GetMapping("/dashboard/activity-trend")
    public Result<?> getActivityTrend() {
        return Result.success(postService.getPostActivityTrend());
    }

    @GetMapping("/dashboard/content-stats")
    public Result<?> getContentStats() {
        return Result.success(postService.getPostContentStats());
    }

    // ========== 用户管理 ==========
    @GetMapping("/users")
    public Result<?> getUsers() {
        List<User> users = userService.list();
        users.forEach(u -> u.setPassword(null));
        return Result.success(users);
    }

    @PutMapping("/users/{id}")
    public Result<?> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        user.setPassword(null); // 不允许通过此接口修改密码
        userService.updateById(user);
        return Result.success();
    }

    @PutMapping("/users/{id}/status")
    public Result<?> updateUserStatus(@PathVariable Long id, @RequestBody StatusRequest req) {
        userService.updateStatus(id, req.getStatus());
        return Result.success();
    }

    @PutMapping("/users/{id}/role")
    public Result<?> updateUserRole(@PathVariable Long id, @RequestBody RoleRequest req) {
        userService.updateRole(id, req.getRole());
        return Result.success();
    }

    // ========== 帖子管理 ==========
    @GetMapping("/posts")
    public Result<?> getPosts(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        return Result.success(postService.getPosts(page, size, null));
    }

    @GetMapping("/posts/pending")
    public Result<?> getPendingPosts(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(postService.getPendingPosts(page, size));
    }

    @PostMapping("/posts/{id}/moderate")
    public Result<?> moderatePost(@PathVariable Long id, @RequestBody ActionRequest req) {
        postService.moderate(id, req.getAction());
        return Result.success();
    }

    @PostMapping("/posts/{id}/pin")
    public Result<?> togglePostPin(@PathVariable Long id) {
        postService.togglePin(id);
        return Result.success();
    }

    @DeleteMapping("/posts/{id}")
    public Result<?> deletePost(@PathVariable Long id) {
        postService.removeById(id);
        return Result.success();
    }

    // ========== 身份认证 ==========
    @GetMapping("/verifications")
    public Result<?> getVerifications() {
        return Result.success(verificationService.getAllPending());
    }

    @PostMapping("/verifications/{id}/review")
    public Result<?> reviewVerification(@PathVariable Long id, @RequestBody ReviewRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        verificationService.review(id, req.getStatus(), principal.getId(), req.getRejectReason());
        return Result.success();
    }

    // ========== 举报管理 ==========
    @GetMapping("/reports")
    public Result<?> getReports() {
        return Result.success(reportService.getAllReports());
    }

    @PostMapping("/reports/{id}/resolve")
    public Result<?> resolveReport(@PathVariable Long id, @RequestBody ActionRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        reportService.resolve(id, req.getAction(), principal.getId());
        return Result.success();
    }

    // ========== 公告管理 ==========
    @GetMapping("/announcements")
    public Result<?> getAnnouncements() {
        return Result.success(announcementService.getAllAnnouncements());
    }

    @PostMapping("/announcements")
    public Result<?> createAnnouncement(@RequestBody Announcement announcement,
            @AuthenticationPrincipal UserPrincipal principal) {
        announcement.setPublisherId(principal.getId());
        announcement.setViewCount(0);
        announcementService.save(announcement);
        return Result.success(announcement);
    }

    @PutMapping("/announcements/{id}")
    public Result<?> updateAnnouncement(@PathVariable Long id, @RequestBody Announcement announcement) {
        announcement.setId(id);
        announcementService.updateById(announcement);
        return Result.success();
    }

    @PostMapping("/announcements/{id}/pin")
    public Result<?> toggleAnnouncementPin(@PathVariable Long id) {
        announcementService.togglePin(id);
        return Result.success();
    }

    @DeleteMapping("/announcements/{id}")
    public Result<?> deleteAnnouncement(@PathVariable Long id) {
        announcementService.removeById(id);
        return Result.success();
    }

    // ========== 活动管理 ==========
    @GetMapping("/activities")
    public Result<?> getActivities() {
        return Result.success(activityService.getAllActivities());
    }

    @PostMapping("/activities")
    public Result<?> createActivity(@RequestBody Activity activity,
            @AuthenticationPrincipal UserPrincipal principal) {
        activity.setOrganizerId(principal.getId());
        activity.setParticipantCount(0);
        activityService.save(activity);
        return Result.success(activity);
    }

    @PutMapping("/activities/{id}")
    public Result<?> updateActivity(@PathVariable Long id, @RequestBody Activity activity) {
        activity.setId(id);
        activityService.updateById(activity);
        return Result.success();
    }

    @DeleteMapping("/activities/{id}")
    public Result<?> deleteActivity(@PathVariable Long id) {
        activityService.removeById(id);
        return Result.success();
    }

    // ========== 管理员资料 ==========
    @GetMapping("/profile")
    public Result<?> getAdminProfile(@AuthenticationPrincipal UserPrincipal principal) {
        User user = userService.getById(principal.getId());
        user.setPassword(null);
        Map<String, Object> profile = new HashMap<>();
        profile.put("name", user.getNickname());
        profile.put("role", user.getRole().equals("ADMIN") ? "管理员" : "协管员");
        profile.put("avatar", user.getAvatar());
        return Result.success(profile);
    }

    @PutMapping("/profile")
    public Result<?> updateAdminProfile(@AuthenticationPrincipal UserPrincipal principal,
            @RequestBody ProfileRequest req) {
        userService.lambdaUpdate()
                .eq(User::getId, principal.getId())
                .set(req.getName() != null, User::getNickname, req.getName())
                .set(req.getAvatar() != null, User::getAvatar, req.getAvatar())
                .update();
        return Result.success();
    }

    // ========== 通知 ==========
    @GetMapping("/notifications")
    public Result<?> getAdminNotifications() {
        // 简化版：返回系统通知
        return Result.success(List.of());
    }

    // ========== 失物招领管理 ==========
    @GetMapping("/lost-found")
    public Result<?> getLostFoundItems() {
        try {
            // "PINNED" > "NORMAL"，所以倒序排列即可
            return Result.success(lostFoundService.lambdaQuery()
                    .orderByDesc(LostFound::getPriority)
                    .orderByDesc(LostFound::getCreatedAt)
                    .list());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "查询失物招领列表失败: " + e.getMessage());
        }
    }

    // ...
    @PostMapping("/lost-found")
    public Result<?> createLostFoundItem(@RequestBody LostFound item,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("收到创建/保存请求: {}", item);
        if (currentUser != null) {
            item.setUserId(currentUser.getId());
        } else {
            // 兼容本地无Token测试，尝试获取第一个有效用户
            try {
                User randomUser = userService.lambdaQuery().last("LIMIT 1").one();
                if (randomUser != null) {
                    item.setUserId(randomUser.getId());
                } else {
                    // 如果没有用户，尝试创建一个临时管理员（或者是抛出更明确的错误）
                    return Result.error(500, "系统错误：没有可用的用户账户来关联此记录，请先创建用户");
                }
            } catch (Exception e) {
                // 如果userService有问题，回退到ID 1
                item.setUserId(1L);
            }
        }

        if (item.getPriority() == null)
            item.setPriority("NORMAL");
        if (item.getStatus() == null)
            item.setStatus("OPEN");
        if (item.getType() == null)
            item.setType("LOST");

        try {
            lostFoundService.save(item);
            return Result.success(item);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "数据库保存失败: " + e.getMessage());
        }
    }

    @PutMapping("/lost-found/{id}")
    public Result<?> updateLostFoundItem(@PathVariable Long id, @RequestBody LostFound item) {
        item.setId(id);
        lostFoundService.updateById(item);
        return Result.success();
    }

    @PostMapping("/lost-found/{id}/pin")
    public Result<?> toggleLostFoundPin(@PathVariable Long id) {
        LostFound item = lostFoundService.getById(id);
        if (item != null) {
            String priority = "PINNED".equals(item.getPriority()) ? "NORMAL" : "PINNED";
            lostFoundService.lambdaUpdate().eq(LostFound::getId, id).set(LostFound::getPriority, priority).update();
        }
        return Result.success();
    }

    @DeleteMapping("/lost-found/{id}")
    public Result<?> deleteLostFoundItem(@PathVariable Long id) {
        lostFoundService.removeById(id);
        return Result.success();
    }

    // ========== Request DTOs ==========
    @Data
    static class StatusRequest {
        private String status;
    }

    @Data
    static class RoleRequest {
        private String role;
    }

    @Data
    static class ActionRequest {
        private String action;
    }

    @Data
    static class ReviewRequest {
        private String status;
        private String rejectReason;
    }

    @Data
    static class ProfileRequest {
        private String name;
        private String avatar;
    }
}
