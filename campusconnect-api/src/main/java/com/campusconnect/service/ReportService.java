package com.campusconnect.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusconnect.entity.Report;
import com.campusconnect.mapper.ReportMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService extends ServiceImpl<ReportMapper, Report> {
    private final PostService postService;

    public List<Report> getAllReports() {
        return lambdaQuery().orderByDesc(Report::getCreatedAt).list();
    }

    public List<Report> getPendingReports() {
        return lambdaQuery().eq(Report::getStatus, "PENDING").orderByDesc(Report::getCreatedAt).list();
    }

    @Transactional
    public void resolve(Long id, String action, Long handlerId) {
        Report report = getById(id);
        if (report == null)
            return;

        String status = "ignore".equals(action) ? "IGNORED" : "RESOLVED";
        lambdaUpdate().eq(Report::getId, id)
                .set(Report::getStatus, status)
                .set(Report::getAction, action)
                .set(Report::getHandlerId, handlerId)
                .set(Report::getHandledAt, LocalDateTime.now())
                .update();

        // 如果选择处罚，删除对应的帖子
        if ("punish".equals(action) && "POST".equals(report.getTargetType())) {
            postService.removeById(report.getTargetId());
        }
    }
}
