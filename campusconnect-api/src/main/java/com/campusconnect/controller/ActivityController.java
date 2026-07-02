package com.campusconnect.controller;

import com.campusconnect.common.Result;
import com.campusconnect.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    /**
     * 前台活动列表
     */
    @GetMapping
    public Result<?> getActivities() {
        return Result.success(activityService.getActiveActivities());
    }
}