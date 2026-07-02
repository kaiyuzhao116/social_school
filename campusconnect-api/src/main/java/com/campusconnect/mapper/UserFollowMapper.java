package com.campusconnect.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campusconnect.entity.UserFollow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserFollowMapper extends BaseMapper<UserFollow> {

    /**
     * 检查是否已关注
     */
    @Select("SELECT COUNT(*) > 0 FROM user_follow WHERE follower_id = #{followerId} AND following_id = #{followingId}")
    boolean isFollowing(@Param("followerId") Long followerId, @Param("followingId") Long followingId);

    /**
     * 获取用户的关注列表ID
     */
    @Select("SELECT following_id FROM user_follow WHERE follower_id = #{userId}")
    List<Long> getFollowingIds(@Param("userId") Long userId);

    /**
     * 获取用户的粉丝列表ID
     */
    @Select("SELECT follower_id FROM user_follow WHERE following_id = #{userId}")
    List<Long> getFollowerIds(@Param("userId") Long userId);
}
