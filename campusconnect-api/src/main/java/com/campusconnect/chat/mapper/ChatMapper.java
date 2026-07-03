package com.campusconnect.chat.mapper;

import com.campusconnect.chat.entity.ChatConversation;
import com.campusconnect.chat.entity.ChatMessage;
import com.campusconnect.chat.vo.ChatMessageVO;
import com.campusconnect.chat.vo.ChatRoomVO;
import com.campusconnect.chat.vo.PrivateChatUserVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ChatMapper {

    /**
     * 查询当前用户加入的聊天室列表
     */
    @Select("""
    SELECT
        c.id AS roomId,
        c.type AS type,
        CASE
            WHEN c.type = 1 THEN COALESCE(u.nickname, '私聊用户')
            ELSE c.name
        END AS roomName,
        c.avatar AS avatar,
        m.unread_count AS unreadCount,
        c.last_message_content AS lastMessageContent,
        DATE_FORMAT(c.last_message_time, '%H:%i') AS lastMessageTime
    FROM chat_conversation c
    JOIN chat_conversation_member m
        ON c.id = m.conversation_id
    LEFT JOIN chat_conversation_member other_m
        ON c.type = 1
       AND c.id = other_m.conversation_id
       AND other_m.user_id <> #{userId}
       AND other_m.status = 1
    LEFT JOIN user u
        ON other_m.user_id = u.id
    WHERE m.user_id = #{userId}
      AND m.status = 1
      AND c.status = 1
    ORDER BY c.last_message_time DESC, c.update_time DESC
""")
    List<ChatRoomVO> selectChatRoomsByUserId(@Param("userId") Long userId);
    @Select("""
    SELECT
        msg.id AS messageId,
        msg.conversation_id AS conversationId,
        msg.sender_id AS senderId,
        u.nickname AS username,
        CASE
            WHEN msg.burned = 1 THEN '消息已焚毁'
            ELSE msg.content
        END AS content,
        msg.message_type AS messageType,
        msg.burn_after_read AS burnAfterRead,
        msg.burned AS burned,
        DATE_FORMAT(msg.send_time, '%Y-%m-%d') AS messageDate,
        DATE_FORMAT(msg.send_time, '%H:%i') AS messageTime
    FROM chat_message msg
    LEFT JOIN user u
        ON msg.sender_id = u.id
    WHERE msg.conversation_id = #{conversationId}
      AND msg.status = 1
    ORDER BY msg.send_time ASC
""")
    List<ChatMessageVO> selectMessagesByConversationId(@Param("conversationId") Long conversationId);

    /**
     * 查询用户是否在该聊天室中
     */
    @Select("""
        SELECT COUNT(1)
        FROM chat_conversation_member
        WHERE conversation_id = #{conversationId}
          AND user_id = #{userId}
          AND status = 1
    """)
    Integer countConversationMember(@Param("conversationId") Long conversationId,
                                    @Param("userId") Long userId);

    /**
     * 插入聊天消息
     */
    @Insert("""
    INSERT INTO chat_message
    (conversation_id, sender_id, client_msg_id, message_type, burn_after_read, burned, content, status, send_time)
    VALUES
    (#{conversationId}, #{senderId}, #{clientMsgId}, #{messageType}, #{burnAfterRead}, #{burned}, #{content}, #{status}, #{sendTime})
""")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertChatMessage(ChatMessage chatMessage);

    /**
     * 更新会话最后一条消息
     */
    @Update("""
        UPDATE chat_conversation
        SET last_message_id = #{messageId},
            last_message_content = #{content},
            last_message_time = NOW(),
            update_time = NOW()
        WHERE id = #{conversationId}
    """)
    int updateConversationLastMessage(@Param("conversationId") Long conversationId,
                                      @Param("messageId") Long messageId,
                                      @Param("content") String content);

    /**
     * 给其他成员未读数 +1
     */
    @Update("""
        UPDATE chat_conversation_member
        SET unread_count = unread_count + 1,
            update_time = NOW()
        WHERE conversation_id = #{conversationId}
          AND user_id <> #{senderId}
          AND status = 1
    """)
    int increaseUnreadCount(@Param("conversationId") Long conversationId,
                            @Param("senderId") Long senderId);

    /**
     * 根据消息ID查询消息详情
     */
    @Select("""
    SELECT
        msg.id AS messageId,
        msg.conversation_id AS conversationId,
        msg.sender_id AS senderId,
        u.nickname AS username,
        CASE
            WHEN msg.burned = 1 THEN '消息已焚毁'
            ELSE msg.content
        END AS content,
        msg.message_type AS messageType,
        msg.burn_after_read AS burnAfterRead,
        msg.burned AS burned,
        DATE_FORMAT(msg.send_time, '%Y-%m-%d') AS messageDate,
        DATE_FORMAT(msg.send_time, '%H:%i') AS messageTime
    FROM chat_message msg
    LEFT JOIN user u
        ON msg.sender_id = u.id
    WHERE msg.id = #{messageId}
""")
    ChatMessageVO selectMessageById(@Param("messageId") Long messageId);
    /**
     * 查询某个会话最新消息ID
     */
    @Select("""
    SELECT IFNULL(MAX(id), 0)
    FROM chat_message
    WHERE conversation_id = #{conversationId}
      AND status = 1
""")
    Long selectLatestMessageId(@Param("conversationId") Long conversationId);
    /**
     * 查询会话内除发送人之外的成员ID
     */
    @Select("""
    SELECT user_id
    FROM chat_conversation_member
    WHERE conversation_id = #{conversationId}
      AND user_id <> #{senderId}
      AND status = 1
""")
    List<Long> selectOtherMemberIds(@Param("conversationId") Long conversationId,
                                    @Param("senderId") Long senderId);

    /**
     * 标记会话已读
     */
    @Update("""
    UPDATE chat_conversation_member
    SET unread_count = 0,
        last_read_message_id = #{lastReadMessageId},
        update_time = NOW()
    WHERE conversation_id = #{conversationId}
      AND user_id = #{userId}
      AND status = 1
""")
    int markConversationRead(@Param("conversationId") Long conversationId,
                             @Param("userId") Long userId,
                             @Param("lastReadMessageId") Long lastReadMessageId);

    /**
     * 根据 privateKey 查询私聊会话ID
     */
    @Select("""
    SELECT id
    FROM chat_conversation
    WHERE type = 1
      AND private_key = #{privateKey}
      AND status = 1
    LIMIT 1
""")
    Long selectPrivateConversationIdByKey(@Param("privateKey") String privateKey);
    /**
     * 查询可私聊用户列表，排除当前用户
     */
    @Select("""
    SELECT
        id AS userId,
        nickname AS nickname,
        username AS username
    FROM user
    WHERE id <> #{currentUserId}
    ORDER BY id ASC
""")
    List<PrivateChatUserVO> selectPrivateChatUsers(@Param("currentUserId") Long currentUserId);

    /**
     * 创建私聊会话
     */
    @Insert("""
    INSERT INTO chat_conversation
    (type, private_key, name, owner_id, status, create_time, update_time)
    VALUES
    (#{type}, #{privateKey}, #{name}, #{ownerId}, #{status}, NOW(), NOW())
""")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertChatConversation(ChatConversation conversation);


    /**
     * 添加会话成员
     */
    @Insert("""
    INSERT INTO chat_conversation_member
    (conversation_id, user_id, role, status, unread_count, join_time, create_time, update_time)
    VALUES
    (#{conversationId}, #{userId}, #{role}, 1, 0, NOW(), NOW(), NOW())
""")
    int insertConversationMember(@Param("conversationId") Long conversationId,
                                 @Param("userId") Long userId,
                                 @Param("role") Integer role);

    /**
     * 根据消息ID查询消息实体
     */
    @Select("""
    SELECT
        id,
        conversation_id AS conversationId,
        sender_id AS senderId,
        client_msg_id AS clientMsgId,
        message_type AS messageType,
        burn_after_read AS burnAfterRead,
        burned AS burned,
        burned_time AS burnedTime,
        burn_trigger_user_id AS burnTriggerUserId,
        content,
        status,
        send_time AS sendTime
    FROM chat_message
    WHERE id = #{messageId}
      AND status = 1
""")
    ChatMessage selectChatMessageEntityById(@Param("messageId") Long messageId);
    /**
     * 标记阅后即焚消息已焚毁
     */
    @Update("""
    UPDATE chat_message
    SET burned = 1,
        burned_time = NOW(),
        burn_trigger_user_id = #{userId},
        update_time = NOW()
    WHERE id = #{messageId}
      AND burn_after_read = 1
      AND burned = 0
      AND status = 1
""")
    int burnMessageAfterRead(@Param("messageId") Long messageId,
                             @Param("userId") Long userId);


    /**
     * 校验用户是否属于该消息所在会话
     */
    @Select("""
    SELECT COUNT(1)
    FROM chat_message msg
    JOIN chat_conversation_member m
        ON msg.conversation_id = m.conversation_id
    WHERE msg.id = #{messageId}
      AND m.user_id = #{userId}
      AND m.status = 1
      AND msg.status = 1
""")
    Integer countUserCanAccessMessage(@Param("messageId") Long messageId,
                                      @Param("userId") Long userId);
    /**
     * 查询某个用户视角下的会话信息
     */
    @Select("""
    SELECT
        c.id AS roomId,
        c.type AS type,
        CASE
            WHEN c.type = 1 THEN COALESCE(u.nickname, '私聊用户')
            ELSE c.name
        END AS roomName,
        c.avatar AS avatar,
        m.unread_count AS unreadCount,
        c.last_message_content AS lastMessageContent,
        DATE_FORMAT(c.last_message_time, '%H:%i') AS lastMessageTime
    FROM chat_conversation c
    JOIN chat_conversation_member m
        ON c.id = m.conversation_id
    LEFT JOIN chat_conversation_member other_m
        ON c.type = 1
       AND c.id = other_m.conversation_id
       AND other_m.user_id <> #{userId}
       AND other_m.status = 1
    LEFT JOIN user u
        ON other_m.user_id = u.id
    WHERE c.id = #{conversationId}
      AND m.user_id = #{userId}
      AND m.status = 1
      AND c.status = 1
    LIMIT 1
""")
    ChatRoomVO selectChatRoomByIdAndUserId(@Param("conversationId") Long conversationId,
                                           @Param("userId") Long userId);
}