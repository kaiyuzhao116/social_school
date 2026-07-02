package com.campusconnect.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campusconnect.entity.Conversation;
import org.apache.ibatis.annotations.*;

@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {

    /**
     * 查找两个用户之间的私聊会话
     */
    @Select("SELECT c.id FROM conversation c " +
            "JOIN conversation_member m1 ON c.id = m1.conversation_id AND m1.user_id = #{userId1} " +
            "JOIN conversation_member m2 ON c.id = m2.conversation_id AND m2.user_id = #{userId2} " +
            "WHERE c.type = 'PRIVATE' LIMIT 1")
    Long findPrivateConversation(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}
