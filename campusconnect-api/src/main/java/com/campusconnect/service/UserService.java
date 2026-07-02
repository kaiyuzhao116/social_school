package com.campusconnect.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusconnect.entity.User;
import com.campusconnect.entity.UserFollow;
import com.campusconnect.mapper.UserMapper;
import com.campusconnect.mapper.UserFollowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<UserMapper, User> {
    private final PasswordEncoder passwordEncoder;
    private final UserFollowMapper userFollowMapper;

    // 使用 @Lazy 避免循环依赖
    private NotificationService notificationService;

    @org.springframework.beans.factory.annotation.Autowired
    public void setNotificationService(@Lazy NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public User findByUsername(String username) {
        return lambdaQuery().eq(User::getUsername, username).one();
    }

    public User findByPhone(String phone) {
        return lambdaQuery().eq(User::getPhone, phone).one();
    }

    public User findByEmail(String email) {
        return lambdaQuery().eq(User::getEmail, email).one();
    }

    public boolean checkPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    public User createUser(String username, String password, String nickname, String email) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        String displayName = nickname != null ? nickname : username;
        user.setNickname(displayName);
        user.setEmail(email);
        user.setRole("USER");
        user.setStatus("NORMAL");
        user.setVerifyStatus("NONE");
        user.setFollowingCount(0);
        user.setFollowerCount(0);
        user.setPostCount(0);
        // 使用基于用户名的稳定头像URL
        user.setAvatar("https://ui-avatars.com/api/?name=" + encodeUsername(displayName)
                + "&background=6366f1&color=fff&size=200");
        save(user);
        return user;
    }

    private String encodeUsername(String name) {
        try {
            return java.net.URLEncoder.encode(name, "UTF-8");
        } catch (Exception e) {
            return name;
        }
    }

    public void updateStatus(Long userId, String status) {
        lambdaUpdate().eq(User::getId, userId).set(User::getStatus, status).update();
    }

    public void updateRole(Long userId, String role) {
        lambdaUpdate().eq(User::getId, userId).set(User::getRole, role).update();
    }

    public void resetPassword(Long userId, String newPassword) {
        lambdaUpdate().eq(User::getId, userId)
                .set(User::getPassword, passwordEncoder.encode(newPassword))
                .update();
    }

    public IPage<User> searchUsers(String keyword, int page, int size) {
        Page<User> p = new Page<>(page, size);
        return lambdaQuery()
                .like(User::getNickname, keyword)
                .or()
                .like(User::getUsername, keyword)
                .page(p);
    }

    /**
     * 检查是否已关注
     */
    public boolean isFollowing(Long followerId, Long followingId) {
        return userFollowMapper.isFollowing(followerId, followingId);
    }

    /**
     * 关注用户
     */
    @Transactional
    public void followUser(Long followerId, Long followingId) {
        System.out
                .println("[Follow Debug] followUser called: followerId=" + followerId + ", followingId=" + followingId);

        // 检查是否已关注
        boolean alreadyFollowing = userFollowMapper.isFollowing(followerId, followingId);
        System.out.println("[Follow Debug] Already following: " + alreadyFollowing);

        if (alreadyFollowing) {
            System.out.println("[Follow Debug] Already following, returning early");
            return;
        }

        // 插入关注记录
        UserFollow follow = new UserFollow();
        follow.setFollowerId(followerId);
        follow.setFollowingId(followingId);
        int insertResult = userFollowMapper.insert(follow);
        System.out.println("[Follow Debug] Insert result: " + insertResult + ", new follow id: " + follow.getId());

        // 更新关注者的关注数
        boolean updateFollowing = lambdaUpdate().eq(User::getId, followerId)
                .setSql("following_count = following_count + 1").update();
        System.out.println("[Follow Debug] Update following count result: " + updateFollowing);

        // 更新被关注者的粉丝数
        boolean updateFollower = lambdaUpdate().eq(User::getId, followingId)
                .setSql("follower_count = follower_count + 1").update();
        System.out.println("[Follow Debug] Update follower count result: " + updateFollower);

        // 创建通知给被关注者
        User follower = getById(followerId);
        System.out.println("[Follow Debug] Follower user: " + (follower != null ? follower.getNickname() : "null"));
        System.out
                .println("[Follow Debug] NotificationService: " + (notificationService != null ? "available" : "null"));

        if (follower != null && notificationService != null) {
            String followerName = follower.getNickname() != null ? follower.getNickname() : follower.getUsername();
            System.out.println("[Follow Debug] Creating notification for followingId=" + followingId);
            notificationService.createNotification(
                    followingId, // 接收者：被关注的人
                    followerId, // 发送者：关注的人
                    "FOLLOW", // 类型
                    "新粉丝", // 标题
                    followerName + " 关注了你", // 内容
                    followerId, // 目标ID：关注者的用户ID
                    "USER" // 目标类型
            );
            System.out.println("[Follow Debug] Notification created successfully");
        }

        System.out.println("[Follow Debug] followUser completed");
    }

    /**
     * 取消关注
     */
    @Transactional
    public void unfollowUser(Long followerId, Long followingId) {
        // 删除关注记录
        int deleted = userFollowMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserFollow>()
                        .eq(UserFollow::getFollowerId, followerId)
                        .eq(UserFollow::getFollowingId, followingId));

        if (deleted > 0) {
            // 更新关注者的关注数
            lambdaUpdate().eq(User::getId, followerId)
                    .setSql("following_count = GREATEST(following_count - 1, 0)").update();

            // 更新被关注者的粉丝数
            lambdaUpdate().eq(User::getId, followingId)
                    .setSql("follower_count = GREATEST(follower_count - 1, 0)").update();
        }
    }

    /**
     * 获取关注列表
     */
    public IPage<User> getFollowing(Long userId, int page, int size) {
        List<Long> followingIds = userFollowMapper.getFollowingIds(userId);
        if (followingIds.isEmpty()) {
            return new Page<>(page, size);
        }

        Page<User> p = new Page<>(page, size);
        return lambdaQuery().in(User::getId, followingIds).page(p);
    }

    /**
     * 获取粉丝列表
     */
    public IPage<User> getFollowers(Long userId, int page, int size) {
        List<Long> followerIds = userFollowMapper.getFollowerIds(userId);
        if (followerIds.isEmpty()) {
            return new Page<>(page, size);
        }

        Page<User> p = new Page<>(page, size);
        return lambdaQuery().in(User::getId, followerIds).page(p);
    }

    /**
     * 获取用户的关注ID列表
     */
    public List<Long> getFollowingIds(Long userId) {
        return userFollowMapper.getFollowingIds(userId);
    }
}
