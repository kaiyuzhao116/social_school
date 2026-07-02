package com.campusconnect.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campusconnect.entity.ConversationMember;
import org.apache.ibatis.annotations.*;

@Mapper
public interface ConversationMemberMapper extends BaseMapper<ConversationMember> {

    /**
     * 增加未读消息数（排除发送者自己）
     */
    @Update("UPDATE conversation_member SET unread_count = unread_count + 1 " +
            "WHERE conversation_id = #{conversationId} AND user_id != #{excludeUserId}")
    void incrementUnreadCount(@Param("conversationId") Long conversationId,
            @Param("excludeUserId") Long excludeUserId);

    /**
     * 获取用户所有会话的未读消息总数
     */
    @Select("SELECT COALESCE(SUM(unread_count), 0) FROM conversation_member WHERE user_id = #{userId}")
    Integer getTotalUnreadCount(@Param("userId") Long userId);

    /**
     * 获取会话成员数量
     */
    @Select("SELECT COUNT(*) FROM conversation_member WHERE conversation_id = #{conversationId}")
    Integer getMemberCount(@Param("conversationId") Long conversationId);
}
