package com.campusconnect.chat.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ChatWebSocketSessionManager {

    /**
     * 一个用户可能开多个浏览器/多个标签页，所以这里不能只存一个 session
     */
    private final ConcurrentHashMap<Long, Set<WebSocketSession>> userSessionMap = new ConcurrentHashMap<>();

    public void addSession(Long userId, WebSocketSession session) {
        userSessionMap
                .computeIfAbsent(userId, key -> ConcurrentHashMap.newKeySet())
                .add(session);

        session.getAttributes().put("userId", userId);

        log.info("用户 {} WebSocket 连接成功，该用户连接数：{}，当前在线用户数：{}",
                userId,
                userSessionMap.get(userId).size(),
                userSessionMap.size()
        );
    }

    public void removeSession(WebSocketSession session) {
        Object userIdObj = session.getAttributes().get("userId");
        if (userIdObj == null) {
            return;
        }

        Long userId = Long.valueOf(userIdObj.toString());

        Set<WebSocketSession> sessions = userSessionMap.get(userId);
        if (sessions != null) {
            sessions.remove(session);

            if (sessions.isEmpty()) {
                userSessionMap.remove(userId);
            }
        }

        log.info("用户 {} WebSocket 断开连接，当前在线用户数：{}", userId, userSessionMap.size());
    }

    public boolean isOnline(Long userId) {
        Set<WebSocketSession> sessions = userSessionMap.get(userId);

        if (sessions == null || sessions.isEmpty()) {
            return false;
        }

        return sessions.stream().anyMatch(WebSocketSession::isOpen);
    }

    public void sendToUser(Long userId, String message) {
        Set<WebSocketSession> sessions = userSessionMap.get(userId);

        if (sessions == null || sessions.isEmpty()) {
            log.info("用户 {} 不在线，跳过 WebSocket 推送", userId);
            return;
        }

        sessions.removeIf(session -> session == null || !session.isOpen());

        if (sessions.isEmpty()) {
            userSessionMap.remove(userId);
            log.info("用户 {} 没有可用连接，已清理", userId);
            return;
        }

        for (WebSocketSession session : sessions) {
            try {
                synchronized (session) {
                    session.sendMessage(new TextMessage(message));
                }
            } catch (Exception e) {
                log.error("WebSocket 推送失败，userId={}", userId, e);
            }
        }

        log.info("WebSocket 推送完成，userId={}，连接数={}", userId, sessions.size());
    }
}