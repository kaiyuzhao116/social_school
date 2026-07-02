package com.campusconnect.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusconnect.entity.LostFound;
import com.campusconnect.entity.User;
import com.campusconnect.mapper.LostFoundMapper;
import com.campusconnect.service.LostFoundService;
import com.campusconnect.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LostFoundServiceImpl extends ServiceImpl<LostFoundMapper, LostFound> implements LostFoundService {
    private final UserService userService;

    @Override
    public List<LostFound> getRecentItems(int limit) {
        try {
            List<LostFound> items = lambdaQuery()
                    .eq(LostFound::getStatus, "OPEN")
                    .orderByDesc(LostFound::getPriority) // 优先按置顶排序
                    .orderByDesc(LostFound::getCreatedAt)
                    .last("LIMIT " + limit)
                    .list();

            // 填充用户信息
            for (LostFound item : items) {
                if (item.getUserId() != null) {
                    User user = userService.getById(item.getUserId());
                    if (user != null) {
                        user.setPassword(null);
                        item.setUser(user);
                    }
                }
            }

            return items;
        } catch (Exception e) {
            e.printStackTrace(); // 打印异常以便调试
            // 表可能不存在或查询失败，返回空列表
            return List.of();
        }
    }
}
