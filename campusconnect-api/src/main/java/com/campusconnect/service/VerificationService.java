package com.campusconnect.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusconnect.entity.User;
import com.campusconnect.entity.Verification;
import com.campusconnect.mapper.VerificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VerificationService extends ServiceImpl<VerificationMapper, Verification> {
    private final UserService userService;

    public IPage<Verification> getPendingList(int page, int size) {
        Page<Verification> p = new Page<>(page, size);
        IPage<Verification> result = lambdaQuery()
                .eq(Verification::getStatus, "PENDING")
                .orderByAsc(Verification::getCreatedAt)
                .page(p);
        fillUser(result.getRecords());
        return result;
    }

    public List<Verification> getAllPending() {
        List<Verification> list = lambdaQuery().eq(Verification::getStatus, "PENDING").list();
        fillUser(list);
        return list;
    }

    @Transactional
    public void review(Long id, String status, Long reviewerId, String rejectReason) {
        Verification v = getById(id);
        if (v == null) return;

        lambdaUpdate().eq(Verification::getId, id)
                .set(Verification::getStatus, status)
                .set(Verification::getReviewerId, reviewerId)
                .set(Verification::getReviewedAt, LocalDateTime.now())
                .set("REJECTED".equals(status), Verification::getRejectReason, rejectReason)
                .update();

        // 更新用户认证状态和角色
        if ("APPROVED".equals(status)) {
            // 根据认证类型设置用户角色
            String newRole = "USER"; // 默认角色
            String identityType = v.getIdentityType();
            if ("TEACHER".equals(identityType)) {
                newRole = "TEACHER";
            } else if ("ORG".equals(identityType)) {
                newRole = "DEPARTMENT";
            }
            
            userService.lambdaUpdate()
                    .eq(User::getId, v.getUserId())
                    .set(User::getVerifyStatus, "VERIFIED")
                    .set(User::getVerifyType, identityType)
                    .set(User::getRole, newRole)  // 同时更新角色
                    .update();
        }
    }

    private void fillUser(List<Verification> list) {
        for (Verification v : list) {
            User user = userService.getById(v.getUserId());
            if (user != null) {
                user.setPassword(null);
                v.setUser(user);
            }
        }
    }
}
