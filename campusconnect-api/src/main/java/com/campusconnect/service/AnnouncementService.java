package com.campusconnect.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusconnect.entity.Announcement;
import com.campusconnect.mapper.AnnouncementMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnnouncementService extends ServiceImpl<AnnouncementMapper, Announcement> {

    public List<Announcement> getAllAnnouncements() {
        return lambdaQuery()
                .orderByDesc(Announcement::getPriority)
                .orderByDesc(Announcement::getCreatedAt)
                .list();
    }

    public List<Announcement> getPublishedAnnouncements() {
        return lambdaQuery()
                .eq(Announcement::getStatus, "PUBLISHED")
                // 使用 CASE 语句确保 PINNED 排在前面（PINNED=0, NORMAL=1）
                .last("ORDER BY CASE WHEN priority = 'PINNED' THEN 0 ELSE 1 END, published_at DESC")
                .list();
    }

    public void publish(Long id) {
        lambdaUpdate().eq(Announcement::getId, id)
                .set(Announcement::getStatus, "PUBLISHED")
                .set(Announcement::getPublishedAt, LocalDateTime.now())
                .update();
    }

    public void togglePin(Long id) {
        Announcement a = getById(id);
        if (a != null) {
            String priority = "PINNED".equals(a.getPriority()) ? "NORMAL" : "PINNED";
            lambdaUpdate().eq(Announcement::getId, id).set(Announcement::getPriority, priority).update();
        }
    }

    public void incrementViewCount(Long id) {
        lambdaUpdate().eq(Announcement::getId, id)
                .setSql("view_count = view_count + 1")
                .update();
    }
}
