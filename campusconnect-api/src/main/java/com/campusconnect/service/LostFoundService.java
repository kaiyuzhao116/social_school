package com.campusconnect.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campusconnect.entity.LostFound;
import java.util.List;

public interface LostFoundService extends IService<LostFound> {
    /**
     * 获取最新的失物招领列表（公共接口）
     */
    List<LostFound> getRecentItems(int limit);
}
