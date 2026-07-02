package com.campusconnect.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusconnect.entity.Notification;
import com.campusconnect.entity.User;
import com.campusconnect.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService extends ServiceImpl<NotificationMapper, Notification> {
    private final UserService userService;

    public IPage<Notification> getUserNotifications(Long userId, int page, int size) {
        Page<Notification> p = new Page<>(page, size);
        IPage<Notification> result = lambdaQuery()
                .eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreatedAt)
                .page(p);
        
        // 填充发送者信息
        for (Notification n : result.getRecords()) {
            if (n.getSenderId() != null) {
                User sender = userService.getById(n.getSenderId());
                if (sender != null) {
                    sender.setPassword(null);
                    n.setSender(sender);
                }
            }
        }
        return result;
    }

    public long getUnreadCount(Long userId) {
        return lambdaQuery()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, false)
                .count();
    }

    public void markAsRead(Long id, Long userId) {
        lambdaUpdate()
                .eq(Notification::getId, id)
                .eq(Notification::getUserId, userId)
                .set(Notification::getIsRead, true)
                .update();
    }

    public void markAllAsRead(Long userId) {
        lambdaUpdate()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, false)
                .set(Notification::getIsRead, true)
                .update();
    }

    public void deleteByIdAndUserId(Long id, Long userId) {
        lambdaUpdate()
                .eq(Notification::getId, id)
                .eq(Notification::getUserId, userId)
                .remove();
    }

    public void clearAll(Long userId) {
        lambdaUpdate()
                .eq(Notification::getUserId, userId)
                .remove();
    }

    public void createNotification(Long userId, Long senderId, String type, String title, String content, Long targetId, String targetType) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setSenderId(senderId);
        n.setType(type);
        n.setTitle(title);
        n.setContent(content);
        n.setTargetId(targetId);
        n.setTargetType(targetType);
        n.setIsRead(false);
        save(n);
    }
}
