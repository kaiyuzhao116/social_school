package com.campusconnect.ws;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class GroupBuyLiveWebSocketHandler extends TextWebSocketHandler {

    /**
     * 当前在线 WebSocket 连接
     */
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    /**
     * 建立连接
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);

        log.info("【拼团实时推送】WebSocket 连接成功，sessionId={}, 当前在线连接数={}",
                session.getId(),
                sessions.size()
        );

        session.sendMessage(new TextMessage("""
                {"type":"CONNECTED","message":"拼团实时推送连接成功"}
                """));
    }

    /**
     * 断开连接
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);

        log.info("【拼团实时推送】WebSocket 连接关闭，sessionId={}, 当前在线连接数={}",
                session.getId(),
                sessions.size()
        );
    }

    /**
     * 群发消息给所有在线用户
     */
    public void sendToAll(String message) {
        for (WebSocketSession session : sessions) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                }
            } catch (Exception e) {
                log.error("【拼团实时推送】消息发送失败，sessionId={}", session.getId(), e);
            }
        }
    }

    /**
     * 获取当前在线连接数
     */
    public int getOnlineCount() {
        return sessions.size();
    }
}