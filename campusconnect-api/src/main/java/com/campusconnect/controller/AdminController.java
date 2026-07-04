package com.campusconnect.controller;

import com.campusconnect.common.Result;
import com.campusconnect.entity.*;
import com.campusconnect.security.UserPrincipal;
import com.campusconnect.service.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

    /**
     * 真实访问量来自 VisitMetricsFilter 写入 Redis
     */
    private final StringRedisTemplate stringRedisTemplate;
    /**
     * 这个是小时格式化器。
     * 比如现在时间是：
     * 2026-07-05 03:25:30  格式化后就是：2026070503
     * 这个格式会用来拼 Redis key。
     */
    private static final DateTimeFormatter HOUR_KEY_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHH");

    private static final DateTimeFormatter HOUR_NAME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:00");

    // ========== 仪表盘统计 ==========

    /**
     * 数据看板顶部统计：全部来自真实数据库
     */
    @GetMapping("/dashboard/stats")
    public Result<?> getDashboardStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        stats.put("userCount", userService.count());
        stats.put("postCount", postService.count());

        stats.put("pendingVerifications",
                verificationService.lambdaQuery()
                        .eq(Verification::getStatus, "PENDING")
                        .count()
        );

        stats.put("pendingReports",
                reportService.lambdaQuery()
                        .eq(Report::getStatus, "PENDING")
                        .count()
        );

        stats.put("pendingPosts",
                postService.lambdaQuery()
                        .eq(Post::getStatus, "PENDING")
                        .count()
        );

        return Result.success(stats);
    }

    /**
     * 社区活跃度趋势：
     * visits：Redis 中真实接口访问量
     * posts：数据库中每小时真实发帖数
     */
    @GetMapping("/dashboard/activity-trend")
    public Result<?> getActivityTrend() {
        //创建一个列表，用来装最近 7 小时的数据。
        List<Map<String, Object>> trend = new ArrayList<>();
/**这行是把当前时间对齐到整点。
 比如当前时间是：2026-07-05 03:25:36处理后变成：
 2026-07-05 03:00:00
 为什么要这样？因为我们 Redis 是按小时存的。
 key 是：
 2026070503所以读取时也要按小时来算。
 **/
        LocalDateTime currentHour = LocalDateTime.now()
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        //循环最近 7 个小时。
        //如果现在是 03:00，那么 i 从 6 到 0：
        //i = 6 -> 21:00
        //i = 5 -> 22:00
        //i = 4 -> 23:00
        //i = 3 -> 00:00
        //i = 2 -> 01:00
        //i = 1 -> 02:00
        //i = 0 -> 03:00
        for (int i = 6; i >= 0; i--) {
            LocalDateTime hourStart = currentHour.minusHours(i);
            //计算当前小时的结束时间。
            //hourStart = 21:00
            //hourEnd = 22:00
            LocalDateTime hourEnd = hourStart.plusHours(1);
            //把小时转成 Redis key 的时间部分。
            String hourKey = hourStart.format(HOUR_KEY_FORMATTER);
            String redisKey = "campus:metrics:visit:hour:" + hourKey;

            String visitValue = stringRedisTemplate.opsForValue().get(redisKey);
            //long visits = visitValue == null ? 0L : Long.parseLong(visitValue);
            long visits = visitValue == null ? 0L : Long.parseLong(visitValue);
            //统计 21:00 到 22:00 这一小时发了多少帖子
            long posts = postService.lambdaQuery()
                    .ge(Post::getCreatedAt, hourStart)
                    .lt(Post::getCreatedAt, hourEnd)
                    .count();
            //{
            //  "name": "03:00",
            //  "visits": 4517,
            //  "posts": 1
            //}
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", hourStart.format(HOUR_NAME_FORMATTER));
            item.put("visits", visits);
            item.put("posts", posts);

            trend.add(item);
        }

        return Result.success(trend);
    }

    /**
     * 内容分类统计：
     * 从 post.tags 字段真实统计，不再写死假数据
     */
    @GetMapping("/dashboard/content-stats")
    public Result<?> getContentStats() {
        List<Post> posts = postService.lambdaQuery()
                .isNotNull(Post::getTags)
                .ne(Post::getTags, "")
                .list();

        Map<String, Integer> counter = new HashMap<>();

        for (Post post : posts) {
            List<String> tags = parseTags(post.getTags());

            for (String tag : tags) {
                if (tag == null || tag.trim().isEmpty()) {
                    continue;
                }

                String cleanTag = tag.trim();
                counter.put(cleanTag, counter.getOrDefault(cleanTag, 0) + 1);
            }
        }

        List<Map<String, Object>> result = counter.entrySet()
                .stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(10)
                .map(entry -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("name", entry.getKey());
                    item.put("posts", entry.getValue());
                    return item;
                })
                .toList();

        return Result.success(result);
    }

    /**
     * 兼容 tags 是 JSON 字符串的情况：
     * ["校园拼团","拼团成功"]
     */
    private List<String> parseTags(String tags) {
        if (tags == null || tags.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String text = tags.trim();

        text = text.replace("[", "")
                .replace("]", "")
                .replace("\"", "")
                .replace("'", "");

        return Arrays.stream(text.split("[,，]"))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .toList();
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
        user.setPassword(null);
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
    public Result<?> getPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return Result.success(postService.getPosts(page, size, null));
    }

    @GetMapping("/posts/pending")
    public Result<?> getPendingPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
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
    public Result<?> reviewVerification(
            @PathVariable Long id,
            @RequestBody ReviewRequest req,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        verificationService.review(id, req.getStatus(), principal.getId(), req.getRejectReason());
        return Result.success();
    }

    // ========== 举报管理 ==========

    @GetMapping("/reports")
    public Result<?> getReports() {
        return Result.success(reportService.getAllReports());
    }

    @PostMapping("/reports/{id}/resolve")
    public Result<?> resolveReport(
            @PathVariable Long id,
            @RequestBody ActionRequest req,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        reportService.resolve(id, req.getAction(), principal.getId());
        return Result.success();
    }

    // ========== 公告管理 ==========

    @GetMapping("/announcements")
    public Result<?> getAnnouncements() {
        return Result.success(announcementService.getAllAnnouncements());
    }

    @PostMapping("/announcements")
    public Result<?> createAnnouncement(
            @RequestBody Announcement announcement,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        announcement.setPublisherId(principal.getId());
        announcement.setViewCount(0);
        announcementService.save(announcement);
        return Result.success(announcement);
    }

    @PutMapping("/announcements/{id}")
    public Result<?> updateAnnouncement(
            @PathVariable Long id,
            @RequestBody Announcement announcement
    ) {
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
    public Result<?> createActivity(
            @RequestBody Activity activity,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        activity.setOrganizerId(principal.getId());
        activity.setParticipantCount(0);
        activityService.save(activity);
        return Result.success(activity);
    }

    @PutMapping("/activities/{id}")
    public Result<?> updateActivity(
            @PathVariable Long id,
            @RequestBody Activity activity
    ) {
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
    public Result<?> updateAdminProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody ProfileRequest req
    ) {
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
        return Result.success(List.of());
    }

    // ========== 失物招领管理 ==========

    @GetMapping("/lost-found")
    public Result<?> getLostFoundItems() {
        try {
            return Result.success(lostFoundService.lambdaQuery()
                    .orderByDesc(LostFound::getPriority)
                    .orderByDesc(LostFound::getCreatedAt)
                    .list());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "查询失物招领列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/lost-found")
    public Result<?> createLostFoundItem(
            @RequestBody LostFound item,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        log.info("收到创建/保存请求: {}", item);

        if (currentUser != null) {
            item.setUserId(currentUser.getId());
        } else {
            try {
                User randomUser = userService.lambdaQuery().last("LIMIT 1").one();
                if (randomUser != null) {
                    item.setUserId(randomUser.getId());
                } else {
                    return Result.error(500, "系统错误：没有可用的用户账户来关联此记录，请先创建用户");
                }
            } catch (Exception e) {
                item.setUserId(1L);
            }
        }

        if (item.getPriority() == null) {
            item.setPriority("NORMAL");
        }

        if (item.getStatus() == null) {
            item.setStatus("OPEN");
        }

        if (item.getType() == null) {
            item.setType("LOST");
        }

        try {
            lostFoundService.save(item);
            return Result.success(item);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "数据库保存失败: " + e.getMessage());
        }
    }

    @PutMapping("/lost-found/{id}")
    public Result<?> updateLostFoundItem(
            @PathVariable Long id,
            @RequestBody LostFound item
    ) {
        item.setId(id);
        lostFoundService.updateById(item);
        return Result.success();
    }

    @PostMapping("/lost-found/{id}/pin")
    public Result<?> toggleLostFoundPin(@PathVariable Long id) {
        LostFound item = lostFoundService.getById(id);

        if (item != null) {
            String priority = "PINNED".equals(item.getPriority()) ? "NORMAL" : "PINNED";
            lostFoundService.lambdaUpdate()
                    .eq(LostFound::getId, id)
                    .set(LostFound::getPriority, priority)
                    .update();
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