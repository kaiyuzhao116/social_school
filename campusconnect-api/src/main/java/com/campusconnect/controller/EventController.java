package com.campusconnect.controller;

import com.campusconnect.common.Result;
import com.campusconnect.entity.Activity;
import com.campusconnect.security.UserPrincipal;
import com.campusconnect.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {
    private final ActivityService activityService;

    @GetMapping
    public Result<?> getEvents(@RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "20") int size,
                                @RequestParam(required = false) String status) {
        return Result.success(activityService.getActivitiesPage(page, size, status));
    }

    @GetMapping("/{id}")
    public Result<?> getEventById(@PathVariable Long id) {
        Activity activity = activityService.getById(id);
        if (activity == null) {
            return Result.notFound("活动不存在");
        }
        return Result.success(activity);
    }

    @PostMapping("/{id}/register")
    public Result<?> registerEvent(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        Activity activity = activityService.getById(id);
        if (activity == null) {
            return Result.notFound("活动不存在");
        }
        if (!"REGISTERING".equals(activity.getStatus())) {
            return Result.error("活动不在报名阶段");
        }
        if (activity.getMaxParticipants() != null && activity.getParticipantCount() >= activity.getMaxParticipants()) {
            return Result.error("报名人数已满");
        }
        
        boolean success = activityService.registerUser(id, principal.getId());
        if (!success) {
            return Result.error("您已报名过此活动");
        }
        return Result.success();
    }

    @DeleteMapping("/{id}/register")
    public Result<?> unregisterEvent(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        activityService.unregisterUser(id, principal.getId());
        return Result.success();
    }

    @GetMapping("/my")
    public Result<?> getMyEvents(@AuthenticationPrincipal UserPrincipal principal,
                                  @RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "20") int size) {
        return Result.success(activityService.getUserRegisteredActivities(principal.getId(), page, size));
    }
}
