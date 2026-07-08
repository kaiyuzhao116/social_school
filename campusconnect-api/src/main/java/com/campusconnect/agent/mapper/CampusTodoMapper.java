package com.campusconnect.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campusconnect.agent.entity.CampusTodo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CampusTodoMapper extends BaseMapper<CampusTodo> {
}