package com.campusconnect.controller;

import com.campusconnect.common.Result;
import com.campusconnect.security.UserPrincipal;
import com.campusconnect.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    public Result<?> getNotifications(@AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(notificationService.getUserNotifications(principal.getId(), page, size));
    }

    @GetMapping("/unread-count")
    public Result<?> getUnreadCount(@AuthenticationPrincipal UserPrincipal principal) {
        long count = notificationService.getUnreadCount(principal.getId());
        Map<String, Long> data = new HashMap<>();
        data.put("count", count);
        return Result.success(data);
    }

    @PutMapping("/{id}/read")
    public Result<?> markAsRead(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        notificationService.markAsRead(id, principal.getId());
        return Result.success();
    }

    @PutMapping("/read-all")
    public Result<?> markAllAsRead(@AuthenticationPrincipal UserPrincipal principal) {
        notificationService.markAllAsRead(principal.getId());
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteNotification(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        notificationService.deleteByIdAndUserId(id, principal.getId());
        return Result.success();
    }

    @DeleteMapping("/clear")
    public Result<?> clearNotifications(@AuthenticationPrincipal UserPrincipal principal) {
        notificationService.clearAll(principal.getId());
        return Result.success();
    }
}
