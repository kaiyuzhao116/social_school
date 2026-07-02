package com.campusconnect.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campusconnect.common.Result;
import com.campusconnect.entity.User;
import com.campusconnect.entity.Verification;
import com.campusconnect.security.UserPrincipal;
import com.campusconnect.service.PostService;
import com.campusconnect.service.UserService;
import com.campusconnect.service.VerificationService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final PostService postService;
    private final VerificationService verificationService;

    @GetMapping("/me")
    public Result<?> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return Result.error("未登录");
        }
        User user = userService.getById(principal.getId());
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(null);

        // 获取当前用户关注的人的ID列表
        java.util.List<Long> followingIds = userService.getFollowingIds(principal.getId());

        // 构建返回结果，包含用户信息和关注列表
        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("following", followingIds);

        return Result.success(result);
    }

    @PutMapping("/me")
    public Result<?> updateCurrentUser(@AuthenticationPrincipal UserPrincipal principal, @RequestBody User user) {
        user.setId(principal.getId());
        user.setPassword(null);
        user.setRole(null);
        user.setStatus(null);
        userService.updateById(user);
        return Result.success();
    }

    @PutMapping("/me/avatar")
    public Result<?> updateAvatar(@AuthenticationPrincipal UserPrincipal principal, @RequestBody AvatarRequest req) {
        userService.lambdaUpdate().eq(User::getId, principal.getId()).set(User::getAvatar, req.avatar).update();
        return Result.success();
    }

    @PutMapping("/me/cover")
    public Result<?> updateCover(@AuthenticationPrincipal UserPrincipal principal, @RequestBody CoverRequest req) {
        userService.lambdaUpdate().eq(User::getId, principal.getId()).set(User::getCoverImage, req.coverImage).update();
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<?> getUserById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.notFound("用户不存在");
        }
        user.setPassword(null);
        user.setPhone(null);
        user.setEmail(null);
        return Result.success(user);
    }

    @GetMapping("/search")
    public Result<?> searchUsers(@RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        IPage<User> result = userService.searchUsers(keyword, page, size);
        result.getRecords().forEach(u -> {
            u.setPassword(null);
            u.setPhone(null);
            u.setEmail(null);
        });
        return Result.success(result);
    }

    @GetMapping("/{id}/posts")
    public Result<?> getUserPosts(@PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(postService.getUserPosts(id, page, size));
    }

    @PostMapping("/{id}/follow")
    public Result<?> followUser(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        System.out.println("[Controller Debug] followUser endpoint called: targetId=" + id + ", principalId="
                + (principal != null ? principal.getId() : "null"));
        if (id.equals(principal.getId())) {
            return Result.error("不能关注自己");
        }
        userService.followUser(principal.getId(), id);
        System.out.println("[Controller Debug] followUser completed");
        return Result.success();
    }

    @DeleteMapping("/{id}/follow")
    public Result<?> unfollowUser(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        userService.unfollowUser(principal.getId(), id);
        return Result.success();
    }

    @GetMapping("/{id}/following")
    public Result<?> getFollowing(@PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(userService.getFollowing(id, page, size));
    }

    @GetMapping("/{id}/followers")
    public Result<?> getFollowers(@PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(userService.getFollowers(id, page, size));
    }

    @GetMapping("/{id}/is-following")
    public Result<?> isFollowing(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return Result.success(false);
        }
        boolean following = userService.isFollowing(principal.getId(), id);
        return Result.success(following);
    }

    // ========== 身份认证 ==========

    // 获取当前用户的认证状态
    @GetMapping("/me/verification")
    public Result<?> getMyVerificationStatus(@AuthenticationPrincipal UserPrincipal principal) {
        Map<String, Object> status = new HashMap<>();

        // 查询用户所有认证记录
        java.util.List<Verification> verifications = verificationService.lambdaQuery()
                .eq(Verification::getUserId, principal.getId())
                .orderByDesc(Verification::getCreatedAt)
                .list();

        // 按类型整理状态
        Map<String, String> typeStatus = new HashMap<>();
        Map<String, Verification> latestByType = new HashMap<>();

        for (Verification v : verifications) {
            String type = v.getIdentityType().toLowerCase();
            if (!latestByType.containsKey(type)) {
                latestByType.put(type, v);
                typeStatus.put(type, v.getStatus().toLowerCase());
            }
        }

        status.put("teacher", typeStatus.getOrDefault("teacher", "none"));
        status.put("department", typeStatus.getOrDefault("org", "none"));
        status.put("student", typeStatus.getOrDefault("student", "none"));
        status.put("history", verifications);

        return Result.success(status);
    }

    // 提交身份认证申请
    @PostMapping("/me/verification")
    public Result<?> submitVerification(@AuthenticationPrincipal UserPrincipal principal,
            @RequestBody VerificationRequest req) {
        // 检查是否有待审核的申请
        long pendingCount = verificationService.lambdaQuery()
                .eq(Verification::getUserId, principal.getId())
                .eq(Verification::getIdentityType, req.getIdentityType())
                .eq(Verification::getStatus, "PENDING")
                .count();

        if (pendingCount > 0) {
            return Result.error("您已有待审核的申请，请等待审核结果");
        }

        Verification v = new Verification();
        v.setUserId(principal.getId());
        v.setIdentityType(req.getIdentityType());
        v.setRealName(req.getRealName());
        v.setIdNumber(req.getIdNumber());
        v.setDepartment(req.getDepartment());
        v.setIdCardImage(req.getIdCardImage());
        v.setStatus("PENDING");

        verificationService.save(v);
        return Result.success(v);
    }

    // 获取认证历史
    @GetMapping("/me/verification/history")
    public Result<?> getVerificationHistory(@AuthenticationPrincipal UserPrincipal principal) {
        java.util.List<Verification> list = verificationService.lambdaQuery()
                .eq(Verification::getUserId, principal.getId())
                .orderByDesc(Verification::getCreatedAt)
                .list();
        return Result.success(list);
    }

    @Data
    static class AvatarRequest {
        private String avatar;
    }

    @Data
    static class CoverRequest {
        private String coverImage;
    }

    @Data
    static class VerificationRequest {
        private String identityType; // STUDENT, TEACHER, ORG
        private String realName;
        private String idNumber; // 学号/工号
        private String department; // 院系/部门
        private String idCardImage; // 证件照片URL
    }
}
