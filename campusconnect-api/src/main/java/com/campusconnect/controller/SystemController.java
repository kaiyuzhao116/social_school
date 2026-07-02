package com.campusconnect.controller;

import com.campusconnect.common.Result;
import com.campusconnect.service.AnnouncementService;
import com.campusconnect.service.LostFoundService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/system")
@RequiredArgsConstructor
public class SystemController {
    private final AnnouncementService announcementService;
    private final LostFoundService lostFoundService;

    @GetMapping("/config")
    public Result<?> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("appName", "CampusConnect");
        config.put("version", "1.0.0");
        config.put("maxFileSize", 10 * 1024 * 1024); // 10MB
        config.put("allowedImageTypes", new String[] { "image/jpeg", "image/png", "image/gif" });
        return Result.success(config);
    }

    @GetMapping("/announcements")
    public Result<?> getAnnouncements() {
        return Result.success(announcementService.getPublishedAnnouncements());
    }

    @GetMapping("/lost-found")
    public Result<?> getLostFound(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(lostFoundService.getRecentItems(limit));
    }

    @PostMapping("/feedback")
    public Result<?> submitFeedback(@RequestBody FeedbackRequest req) {
        // 简单处理反馈，实际可以存入数据库
        return Result.success("反馈提交成功");
    }

    @Data
    static class FeedbackRequest {
        private String type;
        private String content;
        private String contact;
    }
}
